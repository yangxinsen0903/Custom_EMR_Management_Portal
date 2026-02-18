package sunbox.sdp.ambari.client.model.customaction;


import com.alibaba.fastjson.JSON;
import org.junit.Test;

import java.util.Arrays;
import java.util.Map;

/**
 * @author: wangda
 * @date: 2023/2/26
 */
public class RegionServerDrainInfoTest {

    @Test
    public void testToDrainOn() {
        RegionServerDrainInfo info = RegionServerDrainInfo.of(1, "clusterName", Arrays.asList("host1", "host2"));
        Map<String, Object> map = info.toDrainOnMap();
        System.out.println(JSON.toJSONString(map));
    }

    @Test
    public void testToDrainOff() {
        RegionServerDrainInfo info = RegionServerDrainInfo.of(1, "clusterName", Arrays.asList("host1", "host2"));
        Map<String, Object> map = info.toDrainOffMap();
        System.out.println(JSON.toJSONString(map));
    }
}