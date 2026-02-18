package com.sunbox.sdpcompose.service.ambari.enums;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.sunbox.sdpcompose.service.ambari.enums.BDService.*;

/**
 * 配置分类，也就是各个配置文件
 *
 * @author: wangda
 * @date: 2023/2/10
 */
public enum ConfigClassification {
    CORE_SITE(HDFS, "core-site", "core-site"),
    HADOOP_ENV(HDFS, "hadoop-env", "hadoop-env"),
    HADOOP_METRICS2(HDFS, "hadoop-metrics2.properties", "hadoop-metrics2.properties"),
    HADOOP_POLICY(HDFS, "hadoop-policy", "hadoop-policy"),
    HDFS_LOG4J(HDFS, "hdfs-log4j", "hdfs-log4j"),
    HDFS_SITE(HDFS, "hdfs-site", "hdfs-site"),
    LLAP_CLI_LOG4J2(HDFS, "llap-cli-log4j2", "llap-cli-log4j2"),
    LLAP_DAEMON_LOG4J(HDFS, "llap-daemon-log4j", "llap-daemon-log4j"),
    RANGER_HDFS_AUDIT(HDFS, "ranger-hdfs-audit", "ranger-hdfs-audit"),
    RANGER_HDFS_PLUGIN_PROPERTIES(HDFS, "ranger-hdfs-plugin-properties", "ranger-hdfs-plugin-properties"),
    RANGER_HDFS_POLICYMGR_SSL(HDFS, "ranger-hdfs-policymgr-ssl", "ranger-hdfs-policymgr-ssl"),
    RANGER_HDFS_SECURITY(HDFS, "ranger-hdfs-security", "ranger-hdfs-security"),
    RESOURCE_TYPES(HDFS, "resource-types", "resource-types"),
    SSL_SERVER(HDFS, "ssl-server", "ssl-server"),
    VIEWFS_MOUNT_TABLE(HDFS, "viewfs-mount-table", "viewfs-mount-table"),
    BEELINE_LOG4J2(HIVE, "beeline-log4j2", "beeline-log4j2"),
    HIVE_ATLAS_APPLICATION(HIVE, "hive-atlas-application.properties", "hive-atlas-application.properties"),
    HIVE_ENV(HIVE, "hive-env", "hive-env"),
    HIVE_EXEC_LOG4J2(HIVE, "hive-exec-log4j2", "hive-exec-log4j2"),
    HIVE_INTERACTIVE_ENV(HIVE, "hive-interactive-env", "hive-interactive-env"),
    HIVE_INTERACTIVE_SITE(HIVE, "hive-interactive-site", "hive-interactive-site"),
    HIVE_LOG4J2(HIVE, "hive-log4j2", "hive-log4j2"),
    HIVE_SITE(HIVE, "hive-site", "hive-site"),
    HIVEMETASTORE_SITE(HIVE, "hivemetastore-site", "hivemetastore-site"),
    HIVESERVER2_INTERACTIVE_SITE(HIVE, "hiveserver2-interactive-site", "hiveserver2-interactive-site"),
    HIVESERVER2_SITE(HIVE, "hiveserver2-site", "hiveserver2-site"),
    PARQUET_LOGGING(HIVE, "parquet-logging", "parquet-logging"),
    RANGER_HIVE_AUDIT(HIVE, "ranger-hive-audit", "ranger-hive-audit"),
    RANGER_HIVE_PLUGIN_PROPERTIES(HIVE, "ranger-hive-plugin-properties", "ranger-hive-plugin-properties"),
    RANGER_HIVE_POLICYMGR_SSL(HIVE, "ranger-hive-policymgr-ssl", "ranger-hive-policymgr-ssl"),
    RANGER_HIVE_SECURITY(HIVE, "ranger-hive-security", "ranger-hive-security"),
    MAPRED_ENV(MAPREDUCE2, "mapred-env", "mapred-env"),
    MAPRED_SITE(MAPREDUCE2, "mapred-site", "mapred-site"),
    LIVY3_CLIENT_CONF(SPARK3, "livy3-client-conf", "livy3-client-conf"),
    LIVY3_CONF(SPARK3, "livy3-conf", "livy3-conf"),
    LIVY3_ENV(SPARK3, "livy3-env", "livy3-env"),
    LIVY3_LOG4J_PROPERTIES(SPARK3, "livy3-log4j-properties", "livy3-log4j-properties"),
    LIVY3_SPARK_BLACKLIST(SPARK3, "livy3-spark-blacklist", "livy3-spark-blacklist"),
    SPARK3_ATLAS_APPLICATION_PROPERTIES_OVERRIDE(SPARK3, "spark3-atlas-application-properties-override", "spark3-atlas-application-properties-override"),
    SPARK3_DEFAULTS(SPARK3, "spark3-defaults", "spark3-defaults"),
    SPARK3_ENV(SPARK3, "spark3-env", "spark3-env"),
    SPARK3_HIVE_SITE_OVERRIDE(SPARK3, "spark3-hive-site-override", "spark3-hive-site-override"),
    SPARK3_LOG4J_PROPERTIES(SPARK3, "spark3-log4j-properties", "spark3-log4j-properties"),
    SPARK3_METRICS_PROPERTIES(SPARK3, "spark3-metrics-properties", "spark3-metrics-properties"),
    SPARK3_THRIFT_FAIRSCHEDULER(SPARK3, "spark3-thrift-fairscheduler", "spark3-thrift-fairscheduler"),
    SPARK3_THRIFT_SPARKCONF(SPARK3, "spark3-thrift-sparkconf", "spark3-thrift-sparkconf"),
    SQOOP_ATLAS_APPLICATION(SQOOP, "sqoop-atlas-application.properties", "sqoop-atlas-application.properties"),
    SQOOP_ENV(SQOOP, "sqoop-env", "sqoop-env"),
    TEZ_ENV(TEZ, "tez-env", "tez-env"),
    TEZ_INTERACTIVE_SITE(TEZ, "tez-interactive-site", "tez-interactive-site"),
    TEZ_SITE(TEZ, "tez-site", "tez-site"),
    CAPACITY_SCHEDULER(YARN, "capacity-scheduler", "capacity-scheduler"),
    CONTAINER_EXECUTOR(YARN, "container-executor", "container-executor"),
    RANGER_YARN_AUDIT(YARN, "ranger-yarn-audit", "ranger-yarn-audit"),
    RANGER_YARN_PLUGIN_PROPERTIES(YARN, "ranger-yarn-plugin-properties", "ranger-yarn-plugin-properties"),
    RANGER_YARN_POLICYMGR_SSL(YARN, "ranger-yarn-policymgr-ssl", "ranger-yarn-policymgr-ssl"),
    RANGER_YARN_SECURITY(YARN, "ranger-yarn-security", "ranger-yarn-security"),
    YARN_ENV(YARN, "yarn-env", "yarn-env"),
    YARN_LOG4J(YARN, "yarn-log4j", "yarn-log4j"),
    YARN_SITE(YARN, "yarn-site", "yarn-site"),
    ZOO_CFG(ZOOKEEPER, "zoo.cfg", "zoo.cfg"),
    ZOOKEEPER_ENV(ZOOKEEPER, "zookeeper-env", "zookeeper-env"),
    ZOOKEEPER_LOG4J(ZOOKEEPER, "zookeeper-log4j", "zookeeper-log4j"),
    HBASE_ENV(HBASE, "hbase-env", "hbase-env"),
    HBASE_SITE(HBASE, "hbase-site", "hbase-site");

    /**
     * 分类名称
     */
    private String classification;

    /**
     * 配置文件名称
     */
    private String configFile;

    /**
     * 属于哪个大数据服务
     */
    private BDService service;

    ConfigClassification(BDService service, String classification, String configFile) {
        this.service = service;
        this.classification = classification;
        this.configFile = configFile;
    }

    /**
     * 返回所有的配置标识
     *
     * @return
     */
    public static List<String> allConfigClassifications() {
        List<String> classification = new ArrayList<>();
        for (ConfigClassification value : values()) {
            classification.add(value.getClassification());
        }
        return classification;
    }

    /**
     * 将一个配置标识转换为Service
     *
     * @param classification 配置标识
     * @return
     */
    public static BDService parseToService(String classification) {
        for (ConfigClassification value : values()) {
            if (Objects.equals(classification, value.getClassification())) {
                return value.service;
            }
        }

        return null;
    }

    /**
     * 列出某个服务下的所有配置标识
     *
     * @param service 大数据服务的名称
     * @return
     */
    public static List<ConfigClassification> listByService(String service) {
        List<ConfigClassification> result = new ArrayList<>();
        Optional<BDService> svr = BDService.parse(service);
        if (svr.isPresent()) {
            for (ConfigClassification value : values()) {
                if (Objects.equals(svr.get(), value.getService())) {
                    result.add(value);
                }
            }
        }

        return result;
    }

    /**
     * 将一个配置标识转换为Service
     *
     * @param classification 配置标识
     * @return
     */
    public static ConfigClassification parse(String classification) {
        for (ConfigClassification value : values()) {
            if (Objects.equals(classification, value.getClassification())) {
                return value;
            }
        }

        return null;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public String getConfigFile() {
        return configFile;
    }

    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

    public BDService getService() {
        return service;
    }

    public void setService(BDService service) {
        this.service = service;
    }
}
