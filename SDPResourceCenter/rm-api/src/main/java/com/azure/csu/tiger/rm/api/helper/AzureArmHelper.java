package com.azure.csu.tiger.rm.api.helper;

import com.azure.csu.tiger.rm.api.request.VirtualMachineGroup;
import com.azure.csu.tiger.rm.api.request.VmSizeProfile;
import com.azure.csu.tiger.rm.api.utils.ConstantUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Data;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Data
public class AzureArmHelper {

    public static String CREATE_GROUP = "arm_create_group";
    public static String UPDATE_DISK = "arm_disk_update";
    public static String UPDATE_DISK_IOPS_MPBS = "arm_disk_update_iops_mbps";

    public JsonObject getArmObject(String objectName) throws IOException {
        InputStream resourceAsStream = ClassLoader.getSystemResourceAsStream(String.format("arm-template/%s.json", objectName));
        String value = new String(resourceAsStream.readAllBytes(), StandardCharsets.UTF_8);
//        ClassPathResource resource = new ClassPathResource(String.format("arm-template/%s.json", objectName));
//        String value = FileUtils.readFileToString(resource.getFile(), "UTF-8");
        return JsonParser.parseString(value).getAsJsonObject();
    }

    public JsonArray getResourcesArray(JsonObject armObject) {
        return armObject.getAsJsonArray("resources");
    }

    public JsonObject getOneCopiedObject(JsonArray resourcesArray) {
        JsonObject o = resourcesArray.get(0).getAsJsonObject();
        JsonObject copiedObject = JsonParser.parseString(o.toString()).getAsJsonObject();
        return copiedObject;
    }

    public void setResourceBase(String region, String clusterName, VirtualMachineGroup group, JsonObject fleetObject, String sysCreateBatch) {
        String fleetName = ConstantUtil.buildFleetName(clusterName, group.getGroupName());
        String location = region;
        String zone = group.getVirtualMachineSpec().getBaseProfile().getZone();
        String secondZone = group.getVirtualMachineSpec().getBaseProfile().getSecondaryZone();
        Map<String, String> tags = group.getVirtualMachineSpec().getVirtualMachineTags();
        tags.put(ConstantUtil.SYS_SDP_CLUSTER, clusterName);
        tags.put(ConstantUtil.SYS_SDP_GROUP, group.getGroupName());
        tags.put(ConstantUtil.SYS_CREATE_BATCH, sysCreateBatch);
        tags.put(ConstantUtil.SYS_SDP_DNS, group.getVirtualMachineSpec().getBaseProfile().getHostNameSuffix());
        List<String> userAssignedIdentityResourceIds = group.getVirtualMachineSpec().getBaseProfile().getUserAssignedIdentityResourceIds();

        fleetObject.addProperty("name", fleetName);
        fleetObject.addProperty("location", location);
        JsonArray zones = new JsonArray();
        zones.add(zone);
        if (StringUtils.hasText(secondZone)) {
            zones.add(secondZone);
        }
        fleetObject.add("zones", zones);
        fleetObject.add("tags", new Gson().toJsonTree(tags));

        JsonObject identity = fleetObject.getAsJsonObject("identity");
        identity.add("userAssignedIdentities", new Gson().toJsonTree(userAssignedIdentityResourceIds.stream().collect(Collectors.toMap(i -> i, i -> new JsonObject()))));
    }

    public void setVmSizeProfile(VirtualMachineGroup group, JsonArray vmSizeArray) {
        List<VmSizeProfile> vmSizes = group.getVirtualMachineSpec().getVmSizesProfile();
        for (VmSizeProfile vmSize : vmSizes) {
            JsonObject object = new JsonObject();
            object.addProperty("name", vmSize.getName());
            if (vmSize.getRank() != null) {
                object.addProperty("rank", vmSize.getRank());
            }
            vmSizeArray.add(object);
        }
    }

    public void setSpotProfile(VirtualMachineGroup group, JsonObject spotObject) {
        Integer spotCapacity = group.getVirtualMachineSpec().getSpotProfile().getCapacity();
        Integer minCapacity = group.getVirtualMachineSpec().getSpotProfile().getMinCapacity();
        String spotAllocationStrategy = group.getVirtualMachineSpec().getSpotProfile().getAllocationStrategy();
        String spotEvictionPolicy = group.getVirtualMachineSpec().getSpotProfile().getEvictionPolicy();
        boolean spotMaintain = group.getVirtualMachineSpec().getSpotProfile().isMaintain();
        String maxPricePerVM = group.getVirtualMachineSpec().getSpotProfile().getMaxPricePerVM();

        spotObject.addProperty("capacity", spotCapacity);
        spotObject.addProperty("minCapacity", minCapacity);
        spotObject.addProperty("allocationStrategy", spotAllocationStrategy);
        spotObject.addProperty("evictionPolicy", spotEvictionPolicy);
        spotObject.addProperty("maintain", spotMaintain);
        spotObject.addProperty("maxPricePerVM", maxPricePerVM);
    }

    public void setRegularProfile(VirtualMachineGroup group, JsonObject regularObject) {
        Integer regularCapacity = group.getVirtualMachineSpec().getRegularProfile().getCapacity();
        Integer minCapacity = group.getVirtualMachineSpec().getRegularProfile().getMinCapacity();
        String regularAllocationStrategy = group.getVirtualMachineSpec().getRegularProfile().getAllocationStrategy();

        regularObject.addProperty("capacity", regularCapacity);
        regularObject.addProperty("minCapacity", minCapacity);
        regularObject.addProperty("allocationStrategy", regularAllocationStrategy);
    }

    public void setStorageProfile(VirtualMachineGroup group, JsonObject storageObject) {
        String osDiskSku = group.getVirtualMachineSpec().getBaseProfile().getOsDiskSku();
        Integer osDiskSizeGB = group.getVirtualMachineSpec().getBaseProfile().getOsDiskSizeGB();
        String dataDiskSku = group.getVirtualMachineSpec().getBaseProfile().getDataDiskSku();
        Integer dataDiskSizeGB = group.getVirtualMachineSpec().getBaseProfile().getDataDiskSizeGB();
        Integer dataDiskCount = group.getVirtualMachineSpec().getBaseProfile().getDataDiskCount();
        String imageReferenceId = group.getVirtualMachineSpec().getBaseProfile().getCustomOSImageId();
//        Integer dataDiskIOPSReadWrite = group.getVirtualMachineSpec().getBaseProfile().getDataDiskIOPSReadWrite();
//        Integer dataDiskMBpsReadWrite = group.getVirtualMachineSpec().getBaseProfile().getDataDiskMBpsReadWrite();

        storageObject.getAsJsonObject("osDisk").addProperty("diskSizeGB", osDiskSizeGB);
        storageObject.getAsJsonObject("osDisk").getAsJsonObject("managedDisk").addProperty("storageAccountType", osDiskSku);

        JsonArray dataDisksArray = new JsonArray();
        for (int i = 1; i <= dataDiskCount; i++) {
            JsonObject object = new JsonObject();
            object.addProperty("lun", i);
            object.addProperty("createOption", "Empty");
            object.addProperty("deleteOption", "Delete");
            object.addProperty("diskSizeGB", dataDiskSizeGB);
//            if (dataDiskSku.toLowerCase().startsWith("premiumv2")) {
//                object.addProperty("diskIOPSReadWrite", dataDiskIOPSReadWrite);
//                object.addProperty("diskMBpsReadWrite", dataDiskMBpsReadWrite);
//            }
            JsonObject managedDisk = new JsonObject();
            managedDisk.addProperty("storageAccountType", dataDiskSku);
            object.add("managedDisk", managedDisk);
            object.addProperty("deleteOption", "Delete");
            dataDisksArray.add(object);
        }
        storageObject.add("dataDisks", dataDisksArray);

        JsonObject image = new JsonObject();
        image.addProperty("id", imageReferenceId);
        storageObject.add("imageReference", image);
    }

    public void setOsProfile(String clusterName, VirtualMachineGroup group, JsonObject osObject) {
        String adminUserName = group.getVirtualMachineSpec().getBaseProfile().getUserName();
        String keyData = group.getVirtualMachineSpec().getBaseProfile().getSshPublicKey();
        String keyPath = String.format("/home/%s/.ssh/authorized_keys", adminUserName);

        osObject.addProperty("computerNamePrefix", ConstantUtil.buildComputerNamePrefix(clusterName, group.getGroupName()));
        osObject.addProperty("adminUsername", adminUserName);

        JsonObject key = new JsonObject();
        key.addProperty("keyData", keyData);
        key.addProperty("path", keyPath);
        JsonArray publicKeys = new JsonArray();
        publicKeys.add(key);
        osObject.getAsJsonObject("linuxConfiguration").getAsJsonObject("ssh").add("publicKeys", publicKeys);
    }

    public void setNetworkProfile(VirtualMachineGroup group, JsonObject networkObject) {
        String subNetId = group.getVirtualMachineSpec().getBaseProfile().getSubnetResourceId();
        String nsgId = group.getVirtualMachineSpec().getBaseProfile().getNsgResourceId();

        JsonObject interfaceObject = networkObject.getAsJsonArray("networkInterfaceConfigurations").get(0).getAsJsonObject();
        interfaceObject.addProperty("name", group.getGroupName());
        JsonObject ipConfiguration = interfaceObject.getAsJsonObject("properties").getAsJsonArray("ipConfigurations").get(0).getAsJsonObject();
        ipConfiguration.addProperty("name", group.getGroupName());
        ipConfiguration.getAsJsonObject("properties").getAsJsonObject("subnet").addProperty("id", subNetId);
        interfaceObject.getAsJsonObject("properties").getAsJsonObject("networkSecurityGroup").addProperty("id", nsgId);
    }

    public void setExtensionProfile(VirtualMachineGroup group, JsonObject extensionObject) {
        String scriptUrl = group.getVirtualMachineSpec().getBaseProfile().getStartupScriptBlobUrl();
        String[] split = group.getVirtualMachineSpec().getBaseProfile().getStartupScriptBlobUrl().split("/");
        String scriptCommand = String.format("sh %s",split[split.length - 1]);

        JsonArray fileUris = new JsonArray();
        fileUris.add(scriptUrl);
        extensionObject.getAsJsonObject("settings").add("fileUris", fileUris);
        extensionObject.getAsJsonObject("settings").addProperty("commandToExecute",scriptCommand);
    }

}
