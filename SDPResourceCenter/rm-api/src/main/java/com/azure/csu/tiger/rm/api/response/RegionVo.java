package com.azure.csu.tiger.rm.api.response;

import com.azure.resourcemanager.resources.models.Location;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel
@Data
@NoArgsConstructor
public class RegionVo {

    private String key;

    private String name;

    private String displayName;

    private String physicalLocation;

    public static RegionVo from(Location location) {
        RegionVo r = new RegionVo();
        r.setKey(location.key());
        r.setName(location.name());
        r.setDisplayName(location.displayName());
        r.setPhysicalLocation(location.physicalLocation());
        return r;
    }
}
