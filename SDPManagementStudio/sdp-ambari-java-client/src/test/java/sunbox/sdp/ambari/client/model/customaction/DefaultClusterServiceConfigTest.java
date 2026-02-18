package sunbox.sdp.ambari.client.model.customaction;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author: wangda
 * @date: 2023/2/15
 */
public class DefaultClusterServiceConfigTest extends TestCase {

    @Test
    public void testExtractServiceName() {
        DefaultClusterServiceConfig config = new DefaultClusterServiceConfig();
        config.setHref("http://20.172.10.47:8765/api/v1/clusters/sdp3amzDUhwN2U/configurations/service_config_versions?service_name=HBASE&service_config_version=2");
        String service = config.extractServiceName();
        Assert.assertEquals("HBASE", service);
    }
}