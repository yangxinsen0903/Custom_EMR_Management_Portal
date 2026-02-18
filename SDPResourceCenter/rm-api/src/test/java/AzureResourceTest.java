import com.azure.csu.tiger.rm.api.RmApiApplication;
import com.azure.csu.tiger.rm.api.dao.JobDao;
import com.azure.csu.tiger.rm.api.helper.AzureResourceHelper;
import com.azure.csu.tiger.rm.api.jooq.tables.records.SdpRmJobsRecord;
import com.azure.csu.tiger.rm.api.utils.HttpUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = RmApiApplication.class)
public class AzureResourceTest {
    @Autowired
    private AzureResourceHelper azureResourceHelper;
    @Autowired
    private JobDao jobDao;

    @Test
    public void testGetSSHPublicKey() {
        String publicKey = azureResourceHelper.getSshPublicKey("/subscriptions/bba32ad2-4ac4-4bc3-8c34-ad8b2475d857/resourceGroups/rg-sdp-sit-clean-install/providers/Microsoft.KeyVault/vaults/sit-clean-install-kv", "sdp-sit-ssh-public-key");
        System.out.println(publicKey);
    }

    @Test
    public void testGetSSHPublicKey2() {
        String publicKey = azureResourceHelper.getSshPublicKeyVer2("/subscriptions/bba32ad2-4ac4-4bc3-8c34-ad8b2475d857/resourceGroups/rg-sdp-sit-clean-install/providers/Microsoft.KeyVault/vaults/sit-clean-install-kv", "sdp-sit-ssh-public-key");
        System.out.println(publicKey);
    }


    @Test
    public void testGetSubscriptions() {
        azureResourceHelper.getSubscriptions();
    }

    @Test
    public void isClusterDeletedOrDeleting() {
        System.out.println(jobDao.isClusterDeletedOrDeleting("sdp-lxy-10905"));
    }

    @Test
    public void findCreateOrUpdateInProgressJob() {
        List<SdpRmJobsRecord> createOrUpdateInProgressJob = jobDao.findCreateOrUpdateInProgressJob("sdp-lxy-10905");
        System.out.println("yes");
    }
}
