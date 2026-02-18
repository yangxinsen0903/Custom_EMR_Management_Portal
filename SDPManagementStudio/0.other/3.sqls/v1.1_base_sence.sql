/* Create table in target */
CREATE TABLE IF NOT EXISTS sdpms.base_scene(
                                           `scene_id` varchar(50) NOT NULL  COMMENT '场景ID' ,
                                           `cluster_release_ver` varchar(20)   NULL  COMMENT '集群版本号' ,
                                           `scene_name` varchar(100)   NULL  COMMENT '场景名称' ,
                                           `scene_desc` varchar(300)   NULL  COMMENT '场景描述' ,
                                           `created_time` datetime NULL  COMMENT '创建时间' ,
                                           `createdby` varchar(32)   NULL  COMMENT '创建人' ,
                                           PRIMARY KEY (`scene_id`)
) ENGINE=InnoDB DEFAULT CHARSET='utf8mb4' COLLATE='utf8mb4_general_ci' COMMENT='场景基础信息;-';


/* Create table in target */
CREATE TABLE IF NOT EXISTS sdpms.base_scene_apps (
                                                `scene_id` varchar(50) NOT NULL  COMMENT '场景ID' ,
                                                `app_name` varchar(100)   NOT NULL  COMMENT '组件名称' ,
                                                `app_version` varchar(30)  NULL  COMMENT '组件版本号' ,
                                                `required` int NULL  DEFAULT 0 COMMENT '是否必选;1：必选  0：不必选' ,
                                                `sort_no` int NULL  COMMENT '排序序号' ,
                                                PRIMARY KEY (`scene_id`,`app_name`)
) ENGINE=InnoDB DEFAULT CHARSET='utf8mb4' COLLATE='utf8mb4_general_ci' COMMENT='场景应用组件基础信息; -';


-- 初始化场景数据
REPLACE INTO sdpms.base_scene (scene_id,cluster_release_ver,scene_name,scene_desc,created_time,createdby) VALUES
                                                                                                        ('6dee2ab7-6e64-80d2-ca67-2a433db39daa','SDP-1.0','DEFAULT','默认场景','2022-12-21 22:34:00.000','system'),
                                                                                                        ('692fc42a-5251-1eb8-f1de-6dbbd3e47529','SDP-1.0','HBASE','HBASE场景','2022-12-21 22:34:00.000','system');
-- 初始化场景的应用

replace INTO sdpms.base_scene_apps (scene_id,app_name,app_version,required,sort_no) VALUES
                                                                                  ('692fc42a-5251-1eb8-f1de-6dbbd3e47529','HBASE','2.4.4',1,5),
                                                                                  ('692fc42a-5251-1eb8-f1de-6dbbd3e47529','HDFS','3.3.2',1,2),
                                                                                  ('692fc42a-5251-1eb8-f1de-6dbbd3e47529','MAPREDUCE2','3.3.2',1,3),
                                                                                  ('692fc42a-5251-1eb8-f1de-6dbbd3e47529','YARN','3.3.2',1,4),
                                                                                  ('692fc42a-5251-1eb8-f1de-6dbbd3e47529','ZOOKEEPER','3.5.7',1,1),
                                                                                  ('6dee2ab7-6e64-80d2-ca67-2a433db39daa','HDFS','3.3.2',1,2),
                                                                                  ('6dee2ab7-6e64-80d2-ca67-2a433db39daa','Hive','3.1.2',0,5),
                                                                                  ('6dee2ab7-6e64-80d2-ca67-2a433db39daa','MAPREDUCE2','3.3.2',1,3),
                                                                                  ('6dee2ab7-6e64-80d2-ca67-2a433db39daa','SPARK3','3.1.2',0,6),
                                                                                  ('6dee2ab7-6e64-80d2-ca67-2a433db39daa','SQOOP','1.4.7',0,7);
replace INTO sdpms.base_scene_apps (scene_id,app_name,app_version,required,sort_no) VALUES
                                                                                  ('6dee2ab7-6e64-80d2-ca67-2a433db39daa','TEZ','0.10.2',0,8),
                                                                                  ('6dee2ab7-6e64-80d2-ca67-2a433db39daa','YARN','3.3.2',1,4),
                                                                                  ('6dee2ab7-6e64-80d2-ca67-2a433db39daa','ZOOKEEPER','3.5.7',1,1);


replace INTO sdpms.base_release_apps (release_version,app_name,app_verison,required,sort_no,created_by,created_time) VALUES
    ('SDP-1.0','HBase','2.4.4',0,8,'sysadmin','2022-12-01 16:43:13');