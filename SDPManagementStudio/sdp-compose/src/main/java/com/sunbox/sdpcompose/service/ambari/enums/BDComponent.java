package com.sunbox.sdpcompose.service.ambari.enums;

import org.bouncycastle.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * 大数据组件
 *
 * @author: wangda
 * @date: 2022/12/31
 */
public enum BDComponent {
    NAMENODE("HDFS", HAScene.ALL),
    HDFS_CLIENT("HDFS", HAScene.ALL),
    DATANODE("HDFS", HAScene.ALL),
    ZKFC("HDFS", HAScene.HA),
    JOURNALNODE("HDFS", HAScene.HA),
    SECONDARY_NAMENODE("HDFS", HAScene.NON_HA),

    ZOOKEEPER_SERVER("ZOOKEEPER", HAScene.ALL),
    ZOOKEEPER_CLIENT("ZOOKEEPER", HAScene.ALL),

    /**
     * MAPREDUCE2
     */
    HISTORYSERVER("MAPREDUCE2", HAScene.ALL),
    MAPREDUCE2_CLIENT("MAPREDUCE2", HAScene.ALL),

    HIVE_SERVER("HIVE", HAScene.ALL),
    HIVE_METASTORE("HIVE", HAScene.ALL),
    HIVE_CLIENT("HIVE", HAScene.ALL),

    RESOURCEMANAGER("YARN", HAScene.ALL),
    YARN_CLIENT("YARN", HAScene.ALL),
    NODEMANAGER("YARN", HAScene.ALL),
    APP_TIMELINE_SERVER("YARN", HAScene.ALL),

    HBASE_MASTER("HBASE", HAScene.ALL),
    HBASE_CLIENT("HBASE", HAScene.ALL),
    HBASE_REGIONSERVER("HBASE", HAScene.ALL),

    SPARK3_THRIFTSERVER("SPARK3", HAScene.ALL),
    SPARK3_CLIENT("SPARK3", HAScene.ALL),
    SPARK3_JOBHISTORYSERVER("SPARK3", HAScene.ALL),

    TEZ_CLIENT("TEZ", HAScene.ALL),

    SQOOP("SQOOP", HAScene.ALL),
    KAFKA_BROKER("KAFKA", HAScene.ALL),
    ELASTICSEARCH_MASTER("ELASTICSEARCH", HAScene.ALL),
    ELASTICSEARCH_DATA("ELASTICSEARCH", HAScene.ALL),
    KIBANA_SERVER("KIBANA", HAScene.ALL),
    FLINK_HISTORYSERVER("FLINK", HAScene.ALL),
    FLINK_CLIENT("FLINK", HAScene.ALL)

    ;


    private static final Logger logger = LoggerFactory.getLogger(BDComponent.class);
    private final String service;

    private HAScene haScene;

    BDComponent(String service, HAScene haScene) {
        this.service = service;
        this.haScene = haScene;
    }

    public static Optional<BDComponent> parse(String name) {
        name = Strings.toUpperCase(name);
        try {
            BDComponent component = BDComponent.valueOf(name);
            return Optional.of(component);
        } catch (Exception ex) {
            logger.error("解析Component名出错，不支持的名称：" + name, ex);
            return Optional.empty();
        }
    }

    public BDService getService() {
        BDService bdService = BDService.parse(this.service).get();
        if (bdService == null) {
            throw new RuntimeException("not found BDService name:" + this.service);
        }
        return bdService;
    }

    public HAScene getHaScene() {
        return haScene;
    }

    public void setHaScene(HAScene haScene) {
        this.haScene = haScene;
    }
}
