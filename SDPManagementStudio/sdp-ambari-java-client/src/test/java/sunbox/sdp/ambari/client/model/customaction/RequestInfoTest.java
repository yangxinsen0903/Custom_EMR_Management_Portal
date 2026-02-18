package sunbox.sdp.ambari.client.model.customaction;

import junit.framework.TestCase;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: wangda
 * @date: 2023/1/8
 */
public class RequestInfoTest extends TestCase {

    public void testSetQueryComponentName() {
        RequestInfo requestInfo = new RequestInfo();
        List<String> list = new ArrayList<>();
        list.add("component1");
        list.add("component2");
        requestInfo.setQueryComponentName(list);

        String expect = "ServiceComponentInfo/component_name.in(component1,component2)";
        Assert.assertEquals(expect, requestInfo.getQuery());

    }

    public void testSetQueryHostName() {
        RequestInfo requestInfo = new RequestInfo();
        List<String> list = new ArrayList<>();
        list.add("host1");
        list.add("host2");
        requestInfo.setQueryHostName(list);

        String expect = "Hosts/host_name.in(host1,host2)";
        Assert.assertEquals(expect, requestInfo.getQuery());
    }
}