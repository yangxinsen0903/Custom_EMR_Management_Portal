package com.sunbox.sdpcompose.service.ambari.enums;

import org.bouncycastle.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static com.sunbox.sdpcompose.service.ambari.enums.BDComponent.*;

/**
 * 大数据服务
 * @author: wangda
 * @date: 2022/12/31
 */
public enum BDService {
    HDFS(NAMENODE, HDFS_CLIENT, DATANODE, ZKFC, JOURNALNODE, SECONDARY_NAMENODE),
    ZOOKEEPER(ZOOKEEPER_SERVER, ZOOKEEPER_CLIENT),
    YARN(RESOURCEMANAGER, YARN_CLIENT, NODEMANAGER, APP_TIMELINE_SERVER),
    MAPREDUCE2(HISTORYSERVER, MAPREDUCE2_CLIENT),
    HIVE(HIVE_SERVER, HIVE_CLIENT, HIVE_METASTORE),
    HBASE(HBASE_MASTER, HBASE_CLIENT, HBASE_REGIONSERVER),
    SPARK3(SPARK3_JOBHISTORYSERVER, SPARK3_THRIFTSERVER, SPARK3_CLIENT),
    TEZ(TEZ_CLIENT),
    SQOOP(BDComponent.SQOOP),
    KAFKA(KAFKA_BROKER),
    ELASTICSEARCH(ELASTICSEARCH_MASTER, ELASTICSEARCH_DATA),
    KIBANA(KIBANA_SERVER),
    FLINK(FLINK_HISTORYSERVER, FLINK_CLIENT)
    ;

    private static Logger logger = LoggerFactory.getLogger(BDService.class);

    public static Optional<BDService> parse(String name) {
        name = Strings.toUpperCase(name);
        try {
            BDService service = BDService.valueOf(name);
            return Optional.of(service);
        } catch (Exception ex) {
            logger.error("解析Service名出错，不支持的名称：" + name, ex);
            return Optional.empty();
        }
    }

    private List<BDComponent> components = new ArrayList<>();

    BDService(BDComponent... components) {
        this.components = Arrays.asList(components);
    }

    public List<BDComponent> getComponents() {
        return components;
    }

    public List<BDComponent> getComponents(HAScene haScene) {
        List<BDComponent> result = new ArrayList<>();
        return components.stream().filter(component -> Objects.equals(component.getHaScene(), haScene)
                || Objects.equals(HAScene.ALL, component.getHaScene()))
                .collect(Collectors.toList());
    }

    public boolean contains(String componentName) {
        Optional<BDComponent> comp = BDComponent.parse(componentName);

        return contains(comp.get());
    }

    public boolean contains(BDComponent component) {
        if (Objects.isNull(component)) {
            return false;
        }

        return this.components.contains(component);
    }

}
