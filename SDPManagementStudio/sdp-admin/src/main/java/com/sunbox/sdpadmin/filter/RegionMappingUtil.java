package com.sunbox.sdpadmin.filter;

import java.util.HashMap;
import java.util.Map;

/**
 * Shein数据中心映射
 */
public class RegionMappingUtil{
    //入参映射
    private static final Map<String, String> regionMappingInMap = new HashMap<>();
    //出参映射
    private static final Map<String, String> regionMappingOutMap = new HashMap<>();

    static {
        regionMappingInMap.put("uswest3", "westus3");
        regionMappingInMap.put("uswest2", "westus2");
        regionMappingInMap.put("uscentral", "centralus");
        //因为出参映射需要从value获取key，所以反着存一次
        regionMappingOutMap.put("westus3", "uswest3");
        regionMappingOutMap.put("westus2", "uswest2");
        regionMappingOutMap.put("centralus", "uscentral");
    }

    /**
     * 入参映射
     * @param region
     * @return
     */
    public static String mappingIn(String region){
        if (regionMappingInMap.containsKey(region)){
            return regionMappingInMap.get(region);
        }
        return region;
    }
    /**
     * 出参映射
     * @param region
     * @return
     */
    public static String mappingOut(String region){
        if (regionMappingOutMap.containsKey(region)){
            return regionMappingOutMap.get(region);
        }
        return region;
    }

}
