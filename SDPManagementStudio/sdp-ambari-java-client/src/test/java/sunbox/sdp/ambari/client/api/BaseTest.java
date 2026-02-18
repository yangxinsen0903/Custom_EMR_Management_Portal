package sunbox.sdp.ambari.client.api;

import org.junit.BeforeClass;
import sunbox.sdp.ambari.client.ApiClient;
import sunbox.sdp.ambari.client.Configuration;

public class BaseTest {

    @BeforeClass
    public static void initClass() {
        ApiClient apiClient = ApiClient.newInstance();
        apiClient.setBasePath("http://1.13.9.130:8080/api/v1");
        apiClient.setUsername("admin");
        apiClient.setPassword("admin");
        Configuration.setDefaultApiClient(apiClient);
    }
}
