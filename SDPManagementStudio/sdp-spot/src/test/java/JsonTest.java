import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.sunbox.sdpspot.model.VmEvictionEvent;
import org.junit.Test;

import java.util.Date;

public class JsonTest {

    @Test
    public void testJson(){

        VmEvictionEvent vmEvictionEvent = new VmEvictionEvent();
        vmEvictionEvent.setVmName("abc");
        vmEvictionEvent.setHostname("def");
        vmEvictionEvent.setEvictTime(null);
        vmEvictionEvent.setRemaining(null);
        vmEvictionEvent.setTime(new Date());
        vmEvictionEvent.setDeleted(false);

        String redisEventValue = JSONObject.toJSONString(vmEvictionEvent);
        VmEvictionEvent vmEvictionEvent2 = JSONUtil.toBean(redisEventValue, VmEvictionEvent.class);
        if(vmEvictionEvent2.getEvictTime() == null){
            System.out.println("vmEvictionEvent2.getEvictTime() == null");
        } else {
            System.out.println("vmEvictionEvent2.getEvictTime() != null");
        }
    }
}
