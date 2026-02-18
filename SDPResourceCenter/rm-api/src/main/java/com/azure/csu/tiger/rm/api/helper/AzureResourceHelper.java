package com.azure.csu.tiger.rm.api.helper;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpResponse;
import com.azure.core.management.Region;
import com.azure.core.management.exception.ManagementException;
import com.azure.core.management.profile.AzureProfile;
import com.azure.csu.tiger.rm.api.exception.RmException;
import com.azure.csu.tiger.rm.api.response.GetVmInfoVo;
import com.azure.csu.tiger.rm.api.utils.ArmUtil;
import com.azure.csu.tiger.rm.api.utils.ConstantUtil;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.compute.models.ComputeResourceType;
import com.azure.resourcemanager.compute.models.ComputeSku;
import com.azure.resourcemanager.compute.models.DiskUpdate;
import com.azure.resourcemanager.keyvault.models.Secret;
import com.azure.resourcemanager.keyvault.models.Vault;
import com.azure.resourcemanager.msi.models.Identity;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.NetworkSecurityGroup;
import com.azure.resourcemanager.network.models.Subnet;
import com.azure.resourcemanager.privatedns.models.ARecordSet;
import com.azure.resourcemanager.privatedns.models.ARecordSets;
import com.azure.resourcemanager.privatedns.models.PrivateDnsZone;
import com.azure.resourcemanager.resources.fluentcore.model.Accepted;
import com.azure.resourcemanager.resources.models.*;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobContainerItem;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class AzureResourceHelper {

    private static final Logger logger = LoggerFactory.getLogger(AzureResourceHelper.class);

    @Autowired
    private TokenCredential tokenCredential;
    @Autowired
    private AzureProfile profile;
    @Autowired
    private ArmUtil armUtil;
    @Autowired
    private AzureResourceGraphHelper azureResourceGraphHelper;
    @Value("${azure.dns-operation-batch-count}")
    private String dnsOperationBatchCount;

    @Value("${azure.dns-operation-batch-interval}")
    private String dnsOperationBatchInterval;

    public void createArmTemplate(JsonObject armObject, String deploymentName, String resourceGroup) throws IOException {
        Accepted<Deployment> deploymentAccepted = ArmUtil.getArmData().deployments()
                .define(deploymentName)
                .withExistingResourceGroup(resourceGroup)
                .withTemplate(armObject.toString())
                .withParameters(new HashMap<>())
                .withMode(DeploymentMode.INCREMENTAL)
                .beginCreate();
        Deployment deployment = deploymentAccepted.getActivationResponse().getValue();
        logger.info("Deployment status: " + deployment.provisioningState());
    }

    public void createResourceGroup(String region, String rgName) {
        ArmUtil.getArmData().resourceGroups()
                .define(rgName)
                .withRegion(Region.fromName(region))
                .create();
    }

    public boolean existResourceGroup(String rgName) {
        return ArmUtil.getArmData().resourceGroups().contain(rgName);
    }

    public ResourceGroup getResourceGroup(String rgName) {
        return ArmUtil.getArmData().resourceGroups().getByName(rgName);
    }

    public void deleteResourceGroup(String rgName) {
        ArmUtil.getArmData().resourceGroups().beginDeleteByName(rgName);
    }

    public boolean existStorageAccount(String storageAccountName) {
        return ArmUtil.getArmData().storageAccounts().list().stream()
                .anyMatch(sa -> sa.name().equals(storageAccountName));
    }

    public List<Location> getSupportedRegions() {
        return ArmUtil.getArmData().getCurrentSubscription().listLocations().stream().collect(Collectors.toList());
    }

    public List<ComputeSku> getSupportedVMSkuList(String region) {
        return ArmUtil.getArmData().computeSkus().listByRegionAndResourceType(Region.fromName(region), ComputeResourceType.VIRTUALMACHINES)
                .stream().collect(Collectors.toList());
//        return ArmUtil.getArmData().computeSkus().listByRegionAndResourceType(Region.fromName(region), ComputeResourceType.VIRTUALMACHINES)
//                .stream().filter(i -> CollectionUtils.isEmpty(i.restrictions())).collect(Collectors.toList());
    }

    public List<ComputeSku> getSupportedDiskSkuList(String region) {
        return ArmUtil.getArmData().computeSkus().listByRegionAndResourceType(Region.fromName(region), ComputeResourceType.DISKS)
                .stream().collect(Collectors.toList());
    }

    public List<Network> getSupportedNetworkList(String region) {
        return ArmUtil.getArmData().networks().list().stream()
                .filter(i -> i.regionName().equals(region))
                .collect(Collectors.toList());
    }

    public Network getNetwork(String networkId) {
        return ArmUtil.getArmData().networks().getById(networkId);
    }

    public List<Subnet> getSupportedSubnetListByVnet(String vnetId) {
        return ArmUtil.getArmData().networks().getById(vnetId)
                .subnets().values().stream()
                .collect(Collectors.toList());
    }

    public List<Subnet> getSupportedSubnetList(String region) {
        return ArmUtil.getArmData().networks().list().stream()
                .filter(i -> i.regionName().equals(region))
                .flatMap(n -> n.subnets().values().stream())
                .collect(Collectors.toList());
    }

    public List<NetworkSecurityGroup> getSupportedNSGSkuList(String region) {
        return ArmUtil.getArmData().networkSecurityGroups().list().stream()
                .filter(i -> i.regionName().equals(region))
                .collect(Collectors.toList());
    }

    public JsonArray getSupportedKeyVaultList(String region) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("resources | where type == \"microsoft.keyvault/vaults\"");
        stringBuilder.append(" | where location =~ \"").append(region).append("\"");
//        stringBuilder.append(" | where properties.enableRbacAuthorization == true");
        stringBuilder.append(" | project id, name, endpoint=properties.vaultUri");
        return azureResourceGraphHelper.executeQuery(stringBuilder.toString(), ArmUtil.getSubData());
//        List<ResourceGroup> rgs = ArmUtil.getArmData().resourceGroups().list().stream()
//                .filter(i -> i.regionName().equals(region))
//                .collect(Collectors.toList());
//        AzureResourceManager azureResourceManager = ArmUtil.getArmData();
//        List<Vault> vaults = rgs.parallelStream().flatMap(rg ->
//            azureResourceManager.vaults().listByResourceGroup(rg.name()).stream()
//                    .filter(i -> i.roleBasedAccessControlEnabled())
//        ).collect(Collectors.toList());
//        return vaults;
    }

    public Vault getKeyVault(String kvId) {
        return ArmUtil.getArmData().vaults().getById(kvId);
    }

    public List<Secret> getSupportedSSHKeyPairListByKeyVault(String kvId) {
        return ArmUtil.getArmData().vaults().getById(kvId)
                .secrets().list().stream()
                .collect(Collectors.toList());
    }

    public Map<String, List<Secret>> getSupportedSSHKeyPairList(String region) {
        List<ResourceGroup> rgs = ArmUtil.getArmData().resourceGroups().list().stream()
                .filter(i -> i.regionName().equals(region))
                .collect(Collectors.toList());
        Map<String, List<Secret>> secrets = Maps.newHashMap();
        AzureResourceManager azureResourceManager = ArmUtil.getArmData();
        rgs.parallelStream().forEach(rg -> {
            Map<String, List<Secret>> datas = azureResourceManager.vaults().listByResourceGroup(rg.name()).stream()
                    .parallel()
                    .filter(i -> i.roleBasedAccessControlEnabled())
                    .collect(Collectors.toMap(v -> v.id(), v -> v.secrets().list().stream().collect(Collectors.toList())));
            secrets.putAll(datas);
        });
        return secrets;
    }

    public List<Identity> getSupportedManagedIdentityList(String region) {
        return ArmUtil.getArmData().identities().list().stream()
                .filter(i -> i.regionName().equals(region))
                .collect(Collectors.toList());
    }

    public List<StorageAccount> getSupportedStorageAccount(String region) {
        return ArmUtil.getArmData().storageAccounts().list().stream()
                .filter(i -> i.regionName().equals(region))
                .collect(Collectors.toList());
    }

    public StorageAccount getStorageAccount(String saId) {
        return ArmUtil.getArmData().storageAccounts().getById(saId);
    }

    public List<BlobContainerItem> getSupportedLogsBlobContainerListBySa(String saName) {
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .endpoint(String.format("https://%s.blob.core.windows.net",saName))
                .credential(tokenCredential)
                .buildClient();
        return blobServiceClient.listBlobContainers().stream().collect(Collectors.toList());
    }

    public List<AvailabilityZoneMappings> getSupportedAvailabilityZoneList(String region) {
        Location location = ArmUtil.getArmData().getCurrentSubscription().getLocationByRegion(Region.fromName(region));
        if (location == null) {
            return Lists.newArrayList();
        }
        return location.innerModel().availabilityZoneMappings();
    }

    public void deleteVirtualMachine(String vmId) {
        ArmUtil.getArmData().virtualMachines().beginDeleteById(vmId, true);
    }

    public void deleteVirtualMachine(String clusterName, String vmName) {
        ArmUtil.getArmData().virtualMachines().beginDeleteByResourceGroup(ConstantUtil.getResourceGroupName(clusterName), vmName, true);
    }

    public void appendSuffixToVirtualMachineComputerName(String resourceGroup, List<String> vmNames, String suffix) {
        ArmUtil.getArmData().virtualMachines().listByResourceGroup(resourceGroup).stream()
                .filter(i -> vmNames.contains(i.name()))
                .forEach(i -> {
//                    i.update().withComputerName(i.computerName() + suffix)
//                            .apply();
                });
    }

    public void getARecord(String dnsName, String dnsZoneName) {
        Optional<PrivateDnsZone> first = armUtil.getAzureResourceManager("dnsZone").privateDnsZones().list().stream().filter(i -> i.name().equals(dnsZoneName)).findFirst();
        if (first.isEmpty()) {
            throw new RmException(HttpStatus.BAD_REQUEST, "Private DNS Zone not found");
        }
        PrivateDnsZone privateDnsZone = first.get();
        ARecordSets aRecordSets = privateDnsZone.aRecordSets();
//        ARecordSet byName = aRecordSets.getByName(dnsName);
//        logger.info("Get A record {} from private DNS zone {}, privateIP {}", byName.name(), dnsZoneName, byName.ipv4Addresses());

        for (ARecordSet set : aRecordSets.list()) {
            if (set.name().toLowerCase().contains(dnsName.toLowerCase())) {
                logger.info("Get A record {} from private DNS zone {}, privateIP {}", set.name(), dnsZoneName, set.ipv4Addresses());
                return;
            }
        }
    }

    private void printResponseHeaderAndBody(HttpResponse response) {
        response.getHeaders().stream().forEach(i -> {
            logger.info("HttpResponse Header: " + i.getName() + " = " + i.getValue());
        });
        String body = response.getBodyAsString().block();
        logger.info("HttpResponse Body: " + body);
    }

    public void registerVirtualMachinesToPrivateDnsWithRetry(List<GetVmInfoVo> vmNames, String dnsZoneName) {
        if (CollectionUtils.isEmpty(vmNames)) {
            logger.info("No VMs to register to private DNS zone");
            return;
        }
        if (!StringUtils.hasText(dnsZoneName)) {
            logger.warn("No private DNS zone name provided");
            return;
        }
        logger.info("Registering VMs to private DNS zone. First VM name is " + vmNames.get(0).getDnsRecord());
        Optional<PrivateDnsZone> first = armUtil.getAzureResourceManager("dnsZone").privateDnsZones().list().stream().filter(i -> i.name().equals(dnsZoneName)).findFirst();
        if (first.isEmpty()) {
            throw new RmException(HttpStatus.BAD_REQUEST, "Private DNS Zone not found. Zone name:" + dnsZoneName);
        }
        PrivateDnsZone privateDnsZone = first.get();
        int batchCount = Integer.valueOf(dnsOperationBatchCount);
        int batchInterval = Integer.valueOf(dnsOperationBatchInterval) * 1000;
        List<List<GetVmInfoVo>> partitions = Lists.partition(vmNames, batchCount);
        logger.info("Total VMs' count is {}, every batch has {} VMs, Partition VMs into {} batches", vmNames.size(), batchCount, partitions.size());
        int count = 0;
        for(List<GetVmInfoVo> partition : partitions) {
            count++;
            long batchStartTime = System.currentTimeMillis();
            registerDnsRecordWithRetry(partition, privateDnsZone);
            if (count == partitions.size()) {
                break;
            }
            long batchEndTime = System.currentTimeMillis();
            long batchDuration = batchEndTime - batchStartTime;

            if (batchDuration < batchInterval) {
                try {
                    Thread.sleep(batchInterval - batchDuration); // Wait for the remaining time
                } catch (InterruptedException e) {
                    logger.error("Thread sleep error", e);
                }
            }
        }
        logger.info("Register VMs to private DNS zone successfully. First VM name is " + vmNames.get(0).getDnsRecord());
    }

    private void registerDnsRecordWithRetry(List<GetVmInfoVo> vmInfos, PrivateDnsZone privateDnsZone) {
        List<String> vmNames = vmInfos.stream().map(GetVmInfoVo::getDnsRecord).collect(Collectors.toList());
        logger.info("Try registering VMs {}, to private DNS zone", vmNames);
        boolean success = true;
        int retryTimes = 0;
        while (retryTimes < ConstantUtil.AZURE_THROTTLE_RETRY_TIMES_DEFAULT) {
            retryTimes++;
            try {
                PrivateDnsZone.Update update = privateDnsZone.update();
                for (GetVmInfoVo vm : vmInfos) {
                    update = update.defineARecordSet(vm.getDnsRecord())
                            .withIPv4Address(vm.getPrivateIp())
                            .withTimeToLive(600)
                            .attach();
                }
                update.apply();
                break;
            } catch (ManagementException ex) {
                if (retryTimes == ConstantUtil.AZURE_THROTTLE_RETRY_TIMES_DEFAULT) {
                    success = false;
                    break;
                }
                if (ex.getResponse().getStatusCode() == 429) {
                    HttpResponse response = ex.getResponse();
                    printResponseHeaderAndBody(response);
                    String retryAfterHeader = response.getHeaderValue(HttpHeaderName.RETRY_AFTER);
                    int retryAfterSeconds;
                    if (retryAfterHeader != null) {
                        retryAfterSeconds = Integer.parseInt(retryAfterHeader);
                        logger.info("Received 429, retrying after " + retryAfterSeconds + " seconds. First VM name is " + vmInfos.get(0).getDnsRecord());
                    } else {
                        retryAfterSeconds = ConstantUtil.AZURE_THROTTLE_WAIT_TIME_SECONDS_DEFAULT;
                        logger.info("Received 429, but no Retry-After header. Retrying after " + retryAfterSeconds + " seconds. First VM name is " + vmInfos.get(0).getDnsRecord());
                    }
                    int delaySeconds = Math.min(60, (int)Math.pow(2, retryTimes) + retryAfterSeconds);
                    logger.info("Retrying after " + (delaySeconds + 1) + " seconds. Attempt: " + retryTimes + ". First VM name is " + vmInfos.get(0).getDnsRecord());
                    try {
                        Thread.sleep((delaySeconds + 1) * 1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    throw ex;
                }
            }
        }
        if (!success) {
            logger.info("Failed to register VMs {}, to private DNS zone", vmNames);
            throw new RmException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to register VMs to private DNS zone. VMs: " + vmNames);
        } else {
            logger.info("Registered VMs {}, to private DNS zone successfully", vmNames);
        }
    }

    public void removeVirtualMachinesFromPrivateDnsWithRetry(List<String> vmNamesOrigin, String dnsZoneName) {
        logger.info("Debug vmNamesOrigin: {}", vmNamesOrigin);
        Set<String> vmNames = Sets.newHashSet(vmNamesOrigin);
        logger.info("Debug vmNamesOrigin: {}", vmNames);
        List<String> dnsNames = vmNames.stream().map(i -> {
            if (i.endsWith(dnsZoneName)) {
                return i.substring(0, i.lastIndexOf("." + dnsZoneName));
            }
            return i;
        }).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(dnsNames)) {
            logger.info("No VMs to remove from private DNS zone");
            return;
        }
        if (!StringUtils.hasText(dnsZoneName)) {
            logger.warn("No private DNS zone name provided");
            return;
        }
        logger.info("Removing VMs from private DNS zone. First VM name is " + dnsNames.get(0));
        Optional<PrivateDnsZone> first = armUtil.getAzureResourceManager("dnsZone").privateDnsZones().list().stream().filter(i -> i.name().equals(dnsZoneName)).findFirst();
        if (first.isEmpty()) {
            throw new RmException(HttpStatus.BAD_REQUEST, "Private DNS Zone not found. Zone name:" + dnsZoneName);
        }
        PrivateDnsZone privateDnsZone = first.get();
        int batchCount = Integer.valueOf(dnsOperationBatchCount);
        int batchInterval = Integer.valueOf(dnsOperationBatchInterval) * 1000;
        List<List<String>> partitions = Lists.partition(dnsNames, batchCount);
        logger.info("Total VMs' count is {}, every batch has {} VMs, Partition VMs into {} batches", dnsNames.size(), batchCount, partitions.size());
        new Thread(new Runnable(){
            @Override
            public void run() {
                for(List<String> partition : partitions) {
                    long batchStartTime = System.currentTimeMillis();
                    removeDnsRecordWithRetry(partition, privateDnsZone);
                    long batchEndTime = System.currentTimeMillis();
                    long batchDuration = batchEndTime - batchStartTime;
                    // must wait for 60 seconds
                    if (batchDuration < batchInterval) {
                        try {
                            Thread.sleep(batchInterval - batchDuration); // Wait for the remaining time
                        } catch (InterruptedException e) {
                            logger.error("Thread sleep error", e);
                        }
                    }
                }
                logger.info("Remove VMs from private DNS zone successfully. First VM name is " + dnsNames.get(0));
            }
        }).start();
    }

    private void removeDnsRecordWithRetry(List<String> dnsNames, PrivateDnsZone privateDnsZone) {
        logger.info("Try removing VMs {}, from private DNS zone", dnsNames);
        boolean success = true;
        int retryTimes = 0;
        while (retryTimes < ConstantUtil.AZURE_THROTTLE_RETRY_TIMES_DEFAULT) {
            retryTimes++;
            try {
                PrivateDnsZone.Update update = privateDnsZone.update();
                for (String dnsName : dnsNames) {
                    update = update.withoutARecordSet(dnsName);
                }
                update.apply();
                break;
            } catch (ManagementException ex) {
                if (retryTimes == ConstantUtil.AZURE_THROTTLE_RETRY_TIMES_DEFAULT) {
                    success = false;
                    break;
                }
                if (ex.getResponse().getStatusCode() == 429) {
                    HttpResponse response = ex.getResponse();
                    printResponseHeaderAndBody(response);
                    String retryAfterHeader = response.getHeaderValue(HttpHeaderName.RETRY_AFTER);
                    int retryAfterSeconds;
                    if (retryAfterHeader != null) {
                        retryAfterSeconds = Integer.parseInt(retryAfterHeader);
                        logger.info("Received 429, retrying after " + retryAfterSeconds + " seconds. First VM name is " + dnsNames.get(0));
                    } else {
                        retryAfterSeconds = ConstantUtil.AZURE_THROTTLE_WAIT_TIME_SECONDS_DEFAULT;
                        logger.info("Received 429, but no Retry-After header. Retrying after " + retryAfterSeconds + " seconds. First VM name is " + dnsNames.get(0));
                    }
                    int delaySeconds = Math.min(60, (int)Math.pow(2, retryTimes) + retryAfterSeconds);
                    logger.info("Retrying after " + (delaySeconds + 1) + " seconds. Attempt: " + retryTimes + ". First VM name is " + dnsNames.get(0));
                    try {
                        Thread.sleep((delaySeconds + 1) * 1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    throw ex;
                }
            }
        }
        if (!success) {
            logger.error("Failed to remove VMs {}, from private DNS zone", dnsNames);
//            throw new RmException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to remove VMs from private DNS zone");
        } else {
            logger.info("Removed VMs {}, from private DNS zone successfully", dnsNames);
        }
    }

    public String getSshPublicKey(String kvId, String secretName) {
        Secret secret = ArmUtil.getArmData().vaults().getById(kvId).secrets().getByName(secretName);
        return secret.getValue();
    }

    public String getSshPublicKeyVer2(String resourceId, String secretName) {
        String[] splits = resourceId.split("/");
        String kvName = splits[splits.length - 1];
        String keyVaultUri = String.format("https://%s.vault.azure.net/", kvName);
        SecretClient secretClient = new SecretClientBuilder().vaultUrl(keyVaultUri).credential(tokenCredential).buildClient();
        KeyVaultSecret retrievedSecret = secretClient.getSecret(secretName);
        return retrievedSecret.getValue();
    }

    public List<HashMap> getSubscriptions() {
        return armUtil.getAzureResourceManager("default").subscriptions().list()
                .stream().map(i -> {
                    HashMap map = new HashMap();
                    map.put("id", i.subscriptionId());
                    map.put("name", i.displayName());
                    return map;
                }).collect(Collectors.toList());
    }

    public void getResourceQuota() {
        List<ComputeSku> supportedVMSkuList = this.getSupportedVMSkuList(Region.US_EAST2.name());
        List<ComputeSku> computeSkuList = supportedVMSkuList.stream().filter(i -> {
            if (i.name().toString().toLowerCase().equals("standard_e4s_v5")) {
                return true;
            }
            return false;
        }).collect(Collectors.toList());
        ArmUtil.getArmData().computeUsages().listByRegion(Region.US_EAST2).forEach(i -> {
            logger.info(i.name().localizedValue() + " : " + i.currentValue());
            System.out.println(i.name().localizedValue() + " : " + i.currentValue());
        });
    }

    public void updateDiskIopsAndMbps(String resourceGroup, String diskName, Integer iops, Integer mbps) {
        if (resourceGroup == null || diskName == null || (iops == null && mbps == null)) {
            return;
        }
        DiskUpdate diskUpdate = new DiskUpdate();
        if (iops != null) {
            diskUpdate.withDiskIopsReadWrite(iops.longValue());
        }
        if (mbps != null) {
            diskUpdate.withDiskMBpsReadWrite(mbps.longValue());
        }
        logger.info("Update disk {} IOPS {} MBps {}", diskName, iops, mbps);
        ArmUtil.getArmData().disks().manager().serviceClient().getDisks().update(resourceGroup, diskName, diskUpdate);
    }
}
