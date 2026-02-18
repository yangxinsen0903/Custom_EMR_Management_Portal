
CREATE TABLE if not exists info_cluster_component_layout(
                                              `id` BIGINT(20) NOT NULL AUTO_INCREMENT  COMMENT '自增主键;自增主键' ,
                                              `cluster_id` VARCHAR(255)    COMMENT '集群ID' ,
                                              `service_code` VARCHAR(64) NOT NULL   COMMENT '大数据服务代码;如：HDFS，YARN，ZOOKEEPER' ,
                                              `host_group` VARCHAR(32) NOT NULL   COMMENT '主机组名称;固定的几个名称：ambari, master, master1, master2, core, task' ,
                                              `component_code` VARCHAR(64) NOT NULL   COMMENT '大数据组件代码;如：NAMENODE，DATANODE' ,
                                              `is_ha` TINYINT(4) NOT NULL   COMMENT '是否高可用;1：高可用；0：非高可用' ,
                                              `state` VARCHAR(16) NOT NULL  DEFAULT 'VALID' COMMENT '状态。如：VALID，INVALID，DELETED' ,
                                              `created_by` VARCHAR(32)    COMMENT '创建人;创建人' ,
                                              `created_time` DATETIME    COMMENT '创建时间;创建时间' ,
                                              `updated_by` VARCHAR(32)    COMMENT '更新人;更新人' ,
                                              `updated_time` DATETIME    COMMENT '更新时间;更新时间' ,
                                              PRIMARY KEY (id)
)  COMMENT = '集群Ambari组件部署布局';
