package com.sunbox.sdpcompose.configuration;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sunbox.domain.InfoDelayMsg;
import com.sunbox.domain.ResultMsg;
import com.sunbox.sdpcompose.listener.DelayedMsgProcess;
import com.sunbox.sdpcompose.mapper.InfoDelayMsgMapper;
import com.sunbox.sdpcompose.mode.queue.DelayedTask;
import com.sunbox.sdpcompose.producer.ProducerCache;
import com.sunbox.sdpcompose.service.IKafkaListener;
import com.sunbox.sdpcompose.listener.KafkaListenerJob;
import com.sunbox.sdpcompose.service.IMQProducerService;
import com.sunbox.web.BaseCommonInterFace;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.DelayQueue;

/**
 * @author : [niyang]
 * @className : KafkaConfiguration
 * @description : [描述说明该类的功能]
 * @createTime : [2023/1/2 1:18 PM]
 */
@Configuration
public class KafkaConfiguration implements BaseCommonInterFace {

    @Value("${kafka.configs:[]}")
    private String kafkaconfigs;

    @Value("${message.compose:servicebus}")
    private String messagecompose;

    @Autowired
    @Qualifier("ComposeListener")
    private IKafkaListener composeListener;

    @Autowired
    @Qualifier("AzureResourceListener")
    private IKafkaListener azureResourceListener;

    @Autowired
    @Qualifier("AnsibleListener")
    private IKafkaListener ansibleListener;


    @Bean
    public boolean initKafka(){
        if (!messagecompose.equalsIgnoreCase("kafka")){
            // 非kafka 消息类型 不做处理
            return false;
        }
        //region 加载客户端配置
        List<KafkaConfig> configs=loadconfig(kafkaconfigs);
        //endregion 加载客户端配置

        //region 初始化客户端
        configs.stream().forEach(c->{
            if (c.getClientType().equalsIgnoreCase(KafkaConfig.CLIENTTYPE_CONSUMER)){
                // 消费者
                IKafkaListener listener=getListener(c.getListenclassname());
                if (listener==null){
                    getLogger().error("listener is null");
                }
                KafkaListenerJob kafkaListenerJob=new KafkaListenerJob(c,listener);
                Thread t=new Thread(kafkaListenerJob);
                t.start();
            }

            if (c.getClientType().equalsIgnoreCase(KafkaConfig.CLIENTTYPE_PRODUCER)){
                // 生产者
                KafkaProducer<String,Object> producer=new KafkaProducer<String, Object>(c.getProperties());

                if (ProducerCache.kafkaProducers==null){
                    ProducerCache.kafkaProducers=new HashMap<>();
                }
                if (ProducerCache.kafkaClientTopic==null){
                    ProducerCache.kafkaClientTopic=new HashMap<>();
                }
                ProducerCache.kafkaProducers.put(c.getClientName(),producer);
                ProducerCache.kafkaClientTopic.put(c.getClientName(),c.getTopic());
            }
        });
        //endregion

        // region 开启队列
        DelayedMsgProcess delayedMsgProcess=new DelayedMsgProcess();
        Thread t=new Thread(delayedMsgProcess);
        t.start();
        // endregion

        return true;
    }


    private IKafkaListener getListener(String classname){
        IKafkaListener iKafkaListener=null;

        if (classname.equalsIgnoreCase("ComposeListener")){
            return composeListener;
        }
        if (classname.equalsIgnoreCase("AzureResourceListener")){
            return azureResourceListener;
        }
        if (classname.equalsIgnoreCase("AnsibleListener")){
            return ansibleListener;
        }
        return iKafkaListener;
    }


    private List<KafkaConfig> loadconfig(String kafkaconfig){

        List<KafkaConfig> configList=new ArrayList<>();

        JSONArray jsonArray=JSONArray.parseArray(kafkaconfig);

        for (int i=0;i<jsonArray.size();i++){
            JSONObject jsonObject=jsonArray.getJSONObject(i);
            KafkaConfig kafkaConfig=new KafkaConfig();
            Properties p=new Properties();

            // kafka配置文件
            JSONObject kafkaconf=jsonObject.getJSONObject("kafka.conf");
            kafkaconf.entrySet().stream().forEach(x->{
                p.put(x.getKey(),x.getValue());
            });

            kafkaConfig.setClientName(jsonObject.getString("clientName"));

            kafkaConfig.setProperties(p);
            kafkaConfig.setTopic(jsonObject.getString("topic"));

            //消费者对应处理逻辑
            if (jsonObject.getString("clientType").equalsIgnoreCase("consumer")) {
                kafkaConfig.setListenclassname(jsonObject.getString("classname"));
                kafkaConfig.setDuration(jsonObject.getLong("duration"));
            }

            kafkaConfig.setClientType(jsonObject.getString("clientType"));
            configList.add(kafkaConfig);
        }
        return configList;
    }

}
