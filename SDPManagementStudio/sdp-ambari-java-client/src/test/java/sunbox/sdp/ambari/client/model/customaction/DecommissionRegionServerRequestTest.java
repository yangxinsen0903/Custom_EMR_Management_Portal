package sunbox.sdp.ambari.client.model.customaction;

import com.alibaba.fastjson.JSON;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author: wangda
 * @date: 2023/2/26
 */
public class DecommissionRegionServerRequestTest {

    @Test
    public void toRequestMap() {
        DecommissionRegionServerRequest request = DecommissionRegionServerRequest.of("sdpCluster-wd001",
                Arrays.asList("sdp-u0qte36bxtk-cor-0002.dev.sdp.com", "sdp-u0qte36bxtk-cor-0003.dev.sdp.com"));

        List<Map> map = request.toRequestMap();
        System.out.println(JSON.toJSONString(map));
    }
}