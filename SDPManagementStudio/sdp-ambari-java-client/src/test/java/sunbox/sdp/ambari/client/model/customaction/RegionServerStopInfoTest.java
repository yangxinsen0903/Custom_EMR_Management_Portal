package sunbox.sdp.ambari.client.model.customaction;

import com.alibaba.fastjson.JSON;
import org.junit.Test;

import java.util.Map;

/**
 * @author: wangda
 * @date: 2023/2/26
 */
public class RegionServerStopInfoTest {

    @Test
    public void toStopMap() {
        RegionServerStopInfo info = RegionServerStopInfo.of(2, "sdpXXXClusterNameXXX", "sdp-u0qte36bxtk-cor-0002.dev.sdp.com");

        Map<String, Object> map = info.toStopMap();
        System.out.println(JSON.toJSONString(map));
    }
}