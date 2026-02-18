package com.sunbox.sdpcompose.configuration;


import java.util.Properties;


public class KafkaConfig {

    /**
     *  kafka属性
     */
     private Properties properties;

    /**
     * 订阅主题
     */
    private String topic;
    /**
     * 消费者逻辑实现类名称
     */
     private String listenclassname;
    /**
     * 订阅pull时间间隔
     */
     private long duration;

    /**
     * 客户端类型 producer customer
     */
     private String clientType;

    /**
     * 客户端名称
     */
    private String clientName;

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public static final String CLIENTTYPE_CONSUMER="consumer";

     public static final String CLIENTTYPE_PRODUCER="producer";

    public String getClientType() {
        return clientType;
    }

    public void setClientType(String clientType) {
        this.clientType = clientType;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getListenclassname() {
        return listenclassname;
    }

    public void setListenclassname(String listenclassname) {
        this.listenclassname = listenclassname;
    }


    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "KafkaConfig{" +
                "properties=" + properties +
                ", topic='" + topic + '\'' +
                ", listenclassname='" + listenclassname + '\'' +
                ", duration=" + duration +
                '}';
    }
}
