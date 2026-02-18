package com.azure.csu.tiger.rm.api.request;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.List;

@ApiModel
@ToString
@Data
@NoArgsConstructor
public class VirtualMachineSpec {

    private List<VmSizeProfile> vmSizesProfile;

    private BaseProfile baseProfile;

    private SpotProfile spotProfile;

    private RegularProfile regularProfile;

    private HashMap<String, String> virtualMachineTags;
}
