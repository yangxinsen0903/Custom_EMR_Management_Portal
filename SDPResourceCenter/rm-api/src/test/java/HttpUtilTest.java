import com.azure.csu.tiger.rm.api.RmApiApplication;
import com.azure.csu.tiger.rm.api.utils.HttpUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = RmApiApplication.class)
public class HttpUtilTest {
    @Autowired
    private HttpUtil httpUtil;

    @Test
    public void testDoGet() {
        String response = httpUtil.doGetFleet("rg-sdp-eric-sdp-cluster", "eric-sdp-cluster-dns-group-2").getRight().toString();
        System.out.println(response);
    }
}
