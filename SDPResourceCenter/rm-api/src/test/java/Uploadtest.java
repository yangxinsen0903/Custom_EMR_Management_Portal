import com.azure.csu.tiger.rm.api.RmApiApplication;
import com.azure.csu.tiger.rm.base.task.UploadLogTask;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = RmApiApplication.class)
public class Uploadtest {

    @Autowired
    private UploadLogTask uploadLogTask;

    @Test
    public void testUploadLog() {
        uploadLogTask.upLoadLogToBlob();
    }
}
