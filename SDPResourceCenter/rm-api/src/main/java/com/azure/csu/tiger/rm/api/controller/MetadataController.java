package com.azure.csu.tiger.rm.api.controller;

import com.azure.core.management.Region;
import com.azure.csu.tiger.rm.api.exception.RmException;
import com.azure.csu.tiger.rm.api.helper.AzureResourceHelper;
import com.azure.csu.tiger.rm.api.request.AddResourceGroupTagsRequest;
import com.azure.csu.tiger.rm.api.request.DeleteResourceGroupTagsRequest;
import com.azure.csu.tiger.rm.api.request.UpdateResourceGroupTagsRequest;
import com.azure.csu.tiger.rm.api.response.*;
import com.azure.csu.tiger.rm.api.service.ResourceGroupService;
import com.azure.csu.tiger.rm.api.utils.JsonUtil;
import com.azure.resourcemanager.compute.models.ComputeSku;
import com.azure.resourcemanager.compute.models.ResourceSkuRestrictions;
import com.azure.resourcemanager.keyvault.models.Secret;
import com.azure.resourcemanager.keyvault.models.Vault;
import com.azure.resourcemanager.msi.models.Identity;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.NetworkSecurityGroup;
import com.azure.resourcemanager.network.models.Subnet;
import com.azure.resourcemanager.resources.models.AvailabilityZoneMappings;
import com.azure.resourcemanager.resources.models.GenericResource;
import com.azure.resourcemanager.resources.models.Location;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.storage.blob.models.BlobContainerItem;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Api(tags="Metadata rest api")
@RequestMapping("/api/v1/metas")
@RestController
public class MetadataController {

    private static final Logger logger = LoggerFactory.getLogger(MetadataController.class);

    @Autowired
    private AzureResourceHelper azureResourceHelper;

    @ApiOperation(value = "查询支持的订阅")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = MetadataResponse.class),
    })
    @GetMapping(path = "/supportedSubscriptionList", produces = {"application/json"})
    public ResponseEntity<MetadataResponse> getSupportedSubscriptionList() {
        List<Object> subList = azureResourceHelper.getSubscriptions().stream().collect(Collectors.toList());
        MetadataResponse response = MetadataResponse.from(subList, null);
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "查询支持的Region")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = MetadataResponse.class),
    })
    @GetMapping(path = "/supportedRegionList", produces = {"application/json"})
    public ResponseEntity<MetadataResponse> getSupportedRegionList() {
        List<Location> supportedRegions = azureResourceHelper.getSupportedRegions();
        List<Object> regionVos = supportedRegions.stream().map(i -> RegionVo.from(i)).collect(Collectors.toList());
        MetadataResponse response = MetadataResponse.from(regionVos, null);
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "查询支持的VM SKU")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = MetadataResponse.class),
    })
    @GetMapping(path = "/supportedVMSkuList/{region}", produces = {"application/json"})
    public ResponseEntity<MetadataResponse> getSupportedVMSkuList(@PathVariable String region) {
        if (!StringUtils.hasText(region)) {
            throw new RmException(HttpStatus.BAD_REQUEST, "region is required");
        }
        region = region.trim();
        List<ComputeSku> supportedVMSkuList = azureResourceHelper.getSupportedVMSkuList(region);
        List<Object> mapList = Lists.newArrayList();
        for (ComputeSku i : supportedVMSkuList) {
            Map<String, Object> pro = new HashMap<>();
            pro.put("name", i.name().toString());
            pro.put("tier", i.tier().toString());
            pro.put("size", i.innerModel().size());
            pro.put("family", i.innerModel().family());
            pro.put("zones", i.zones().get(Region.fromName(region)));
            List<Map> restrictions = Lists.newArrayList();
            for (ResourceSkuRestrictions r : i.restrictions()) {
                Map<String, Object> rMap = new HashMap<>();
                rMap.put("type", r.type());
                rMap.put("restrictLocation", region);
                rMap.put("restrictZones", r.restrictionInfo().zones());
                rMap.put("reasonCode", r.reasonCode().toString());
                restrictions.add(rMap);
            }
            pro.put("restrictions", restrictions);
            Map<String, String> map = i.capabilities().stream().collect(Collectors.toMap(j -> j.name(), j -> j.value()));
            pro.putAll(map);

            mapList.add(pro);
        }
        MetadataResponse response = MetadataResponse.from(mapList, region);
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "查询支持的Disk SKU")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = MetadataResponse.class),
    })
    @GetMapping(path = "/supportedDiskSkuList/{region}", produces = {"application/json"})
    public ResponseEntity<MetadataResponse> getSupportedDiskSkuList(@PathVariable String region) {
        if (!StringUtils.hasText(region)) {
            throw new RmException(HttpStatus.BAD_REQUEST, "region is required");
        }
        region = region.trim();
        List<ComputeSku> supportedVMSkuList = azureResourceHelper.getSupportedDiskSkuList(region);
        List<String> distinctName = supportedVMSkuList.stream().map(i -> i.name().toString()).distinct().collect(Collectors.toList());
        List<Object> mapList = distinctName.stream().map(i -> {
            Map<String, String> pro = new HashMap<>();
            pro.put("name", i);
            return pro;
        }).collect(Collectors.toList());
        MetadataResponse response = MetadataResponse.from(mapList, region);
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "查询支持的VNET")
    @GetMapping(path = "/supportedNetworkList/{region}", produces = {"application/json"})
    public ResponseEntity<MetadataResponse> getSupportedNetworkList(@PathVariable String region) {
        if (!StringUtils.hasText(region)) {
            throw new RmException(HttpStatus.BAD_REQUEST, "region is required");
        }
        region = region.trim();
        List<Network> supportedSubnetList = azureResourceHelper.getSupportedNetworkList(region);
        List<Object> mapList = supportedSubnetList.stream().map(i -> {
            Map<String, String> pro = new HashMap<>();
            pro.put("name", i.name());
            pro.put("resourceId", i.id());
            return pro;
        }).collect(Collectors.toList());
        MetadataResponse response = MetadataResponse.from(mapList, region);
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "查询支持的子网")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = MetadataResponse.class),
    })
    @GetMapping(path = "/supportedSubnetList", produces = {"application/json"})
    public ResponseEntity<MetadataResponse> getSupportedSubnetList(@RequestParam String vnetId) {
        Network vnet = azureResourceHelper.getNetwork(vnetId);
        if (vnet == null) {
            throw new RmException(HttpStatus.BAD_REQUEST, "VNET not found");
        }
        List<Subnet> supportedSubnetList = azureResourceHelper.getSupportedSubnetListByVnet(vnetId);
        String region = vnet.regionName();
        List<Object> mapList = supportedSubnetList.stream().map(i -> {
            Map<String, String> pro = new HashMap<>();
            pro.put("name", i.name());
            pro.put("resourceId", i.id());
            return pro;
        }).collect(Collectors.toList());
        MetadataResponse response = MetadataResponse.from(mapList, region);
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "查询支持的网络安全组")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = MetadataResponse.class),
    })
    @GetMapping(path = "/supportedNSGSkuList/{region}", produces = {"application/json"})
    public ResponseEntity<MetadataResponse> getSupportedNSGSkuList(@PathVariable String region) {
        if (!StringUtils.hasText(region)) {
            throw new RmException(HttpStatus.BAD_REQUEST, "region is required");
        }
        region = region.trim();
        List<NetworkSecurityGroup> supportedNSGSkuList = azureResourceHelper.getSupportedNSGSkuList(region);
        List<Object> mapList = supportedNSGSkuList.stream().map(i -> {
            Map<String, String> pro = new HashMap<>();
            pro.put("name", i.name());
            pro.put("resourceId", i.id());
            return pro;
        }).collect(Collectors.toList());
        MetadataResponse response = MetadataResponse.from(mapList, region);
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "查询支持的Key Vault")
    @GetMapping(path = "/supportedKVList/{region}", produces = {"application/json"})
    public ResponseEntity<MetadataResponse> getSupportedKVList(@PathVariable String region) {
        if (!StringUtils.hasText(region)) {
            throw new RmException(HttpStatus.BAD_REQUEST, "region is required");
        }
        region = region.trim();
        JsonArray datas = azureResourceHelper.getSupportedKeyVaultList(region);
        List<Object> mapList = datas.asList().stream().map(i -> {
            JsonObject data = i.getAsJsonObject();
            Map<String, String> pro = new HashMap<>();
            pro.put("name", data.get("name").getAsString());
            pro.put("resourceId", data.get("id").getAsString());
            pro.put("endpoint", data.get("endpoint").getAsString());
            return pro;
        }).collect(Collectors.toList());
        MetadataResponse response = MetadataResponse.from(mapList, region);
        return ResponseEntity.ok(response);
    }


    @ApiOperation(value = "查询支持的SSH密钥")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = MetadataResponse.class),
    })
    @GetMapping(path = "/supportedSSHKeyPairList", produces = {"application/json"})
    public ResponseEntity<MetadataResponse> getSupportedSSHKeyPairList(@RequestParam String kvId) {
        Vault vault = azureResourceHelper.getKeyVault(kvId);
        if (vault == null) {
            throw new RmException(HttpStatus.BAD_REQUEST, "Key Vault not found");
        }
        List<Secret> supportedSSHKeyPairList = azureResourceHelper.getSupportedSSHKeyPairListByKeyVault(kvId);
        List<Object> mapList = Lists.newArrayList();
        supportedSSHKeyPairList.forEach(i -> {
                Map<String, String> pro = new HashMap<>();
                pro.put("secretName", i.name());
                pro.put("keyVaultResourceId", kvId);
            mapList.add(pro);
        });
        MetadataResponse response = MetadataResponse.from(mapList, vault.regionName());
        return ResponseEntity.ok(response);
    }


    @ApiOperation(value = "查询支持的MI")
    @GetMapping(path = "/supportedManagedIdentityList/{region}", produces = {"application/json"})
    public ResponseEntity<MetadataResponse> getSupportedManagedIdentityList(@PathVariable String region) {
        if (!StringUtils.hasText(region)) {
            throw new RmException(HttpStatus.BAD_REQUEST, "region is required");
        }
        region = region.trim();
        List<Identity> supportedManagedIdentityList = azureResourceHelper.getSupportedManagedIdentityList(region);
        List<Object> mapList = Lists.newArrayList();
        supportedManagedIdentityList.forEach(i -> {
            Map<String, String> pro = new HashMap<>();
            pro.put("name", i.name());
            pro.put("resourceId", i.id());
            pro.put("tenantId", i.tenantId());
            pro.put("principalId", i.principalId());
            pro.put("clientId", i.clientId());
            mapList.add(pro);
        });
        MetadataResponse response = MetadataResponse.from(mapList, region);
        return ResponseEntity.ok(response);
    }


    @ApiOperation(value = "查询支持的可用区")
    @GetMapping(path = "/supportedAvailabilityZoneList/{region}", produces = {"application/json"})
    public ResponseEntity<MetadataResponse> getSupportedAvailabilityZoneList(@PathVariable String region) {
        if (!StringUtils.hasText(region)) {
            throw new RmException(HttpStatus.BAD_REQUEST, "region is required");
        }
        region = region.trim();
        List<AvailabilityZoneMappings> supportedAvailabilityZoneList = azureResourceHelper.getSupportedAvailabilityZoneList(region);
        List<Object> mapList = Lists.newArrayList();
        supportedAvailabilityZoneList.forEach(i -> {
            Map<String, String> pro = new HashMap<>();
            pro.put("logicalZone", i.logicalZone());
            pro.put("physicalZone", i.physicalZone());
            mapList.add(pro);
        });
        MetadataResponse response = MetadataResponse.from(mapList, region);
        return ResponseEntity.ok(response);
    }


    @ApiOperation(value = "查询支持的存储账户")
    @GetMapping(path = "/supportedStorageAccountList/{region}", produces = {"application/json"})
    public ResponseEntity<MetadataResponse> getSupportedStorageAccountList(@PathVariable String region) {
        if (!StringUtils.hasText(region)) {
            throw new RmException(HttpStatus.BAD_REQUEST, "region is required");
        }
        region = region.trim();
        List<StorageAccount> supportedStorageAccount = azureResourceHelper.getSupportedStorageAccount(region);
        List<Object> mapList = Lists.newArrayList();
        supportedStorageAccount.forEach(i -> {
            Map<String, String> pro = new HashMap<>();
            pro.put("name", i.name());
            pro.put("resourceId", i.id());
            mapList.add(pro);
        });
        MetadataResponse response = MetadataResponse.from(mapList, region);
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "查询支持的容器")
    @GetMapping(path = "/supportedLogsBlobContainerList", produces = {"application/json"})
    public ResponseEntity<MetadataResponse> getSupportedLogsBlobContainerList(@RequestParam String saId) {
        StorageAccount storageAccount = azureResourceHelper.getStorageAccount(saId);
        if (storageAccount == null) {
            throw new RmException(HttpStatus.BAD_REQUEST, "Storage Account not found");
        }
        List<BlobContainerItem> supportedLogsBlobContainerList = azureResourceHelper.getSupportedLogsBlobContainerListBySa(storageAccount.name());
        List<Object> mapList = Lists.newArrayList();
        supportedLogsBlobContainerList.forEach(i -> {
            Map<String, String> pro = new HashMap<>();
            pro.put("name", i.getName());
            pro.put("blobContainerUrl", String.format("https://%s.blob.core.windows.net/%s", storageAccount.name(), i.getName()));
            mapList.add(pro);
        });
        MetadataResponse response = MetadataResponse.from(mapList, storageAccount.regionName());
        return ResponseEntity.ok(response);
    }
}
