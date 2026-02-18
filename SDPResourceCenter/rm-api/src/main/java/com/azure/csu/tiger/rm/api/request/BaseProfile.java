package com.azure.csu.tiger.rm.api.request;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@ApiModel
@ToString
@Data
@NoArgsConstructor
public class BaseProfile {

    private String osImageType;

    private String customOSImageId;

    private String marketplaceOSImageName;

    private String hostNameSuffix;

    private String userName;

    private String sshPublicKeyType;

    private String sshKeyVaultId;

    private String sshPublicKeySecretName;

    private String sshPublicKey;

    private String subnetResourceId;

    private String nsgResourceId;

    private String osDiskSku;

    private Integer osDiskSizeGB;

    private String dataDiskSku;

    private Integer dataDiskSizeGB;

    private Integer dataDiskIOPSReadWrite;

    private Integer dataDiskMBpsReadWrite;

    private Integer dataDiskCount;

    private String startupScriptBlobUrl;

    private String zone;

    private String secondaryZone;

    private List<String> userAssignedIdentityResourceIds;
}
