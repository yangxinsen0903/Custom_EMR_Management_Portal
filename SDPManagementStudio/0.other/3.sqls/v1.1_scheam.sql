

CREATE DATABASE  IF NOT EXISTS `sdpms`;

use sdpms;

CREATE TABLE `ambari_component_layout` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键;自增主键',
  `service_code` varchar(64) NOT NULL COMMENT '大数据服务代码;如：HDFS，YARN，ZOOKEEPER',
  `host_group` varchar(32) NOT NULL COMMENT '主机组名称;固定的几个名称：ambari, master, master1, master2, core, task',
  `component_code` varchar(64) NOT NULL COMMENT '大数据组件代码;如：NAMENODE，DATANODE',
  `is_ha` tinyint NOT NULL COMMENT '是否高可用;1：高可用；0：非高可用',
  `state` varchar(16) NOT NULL DEFAULT 'VALID' COMMENT '状态。如：VALID，INVALID，DELETED',
  `created_by` varchar(32) DEFAULT NULL COMMENT '创建人;创建人',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间;创建时间',
  `updated_by` varchar(32) DEFAULT NULL COMMENT '更新人;更新人',
  `updated_time` datetime DEFAULT NULL COMMENT '更新时间;更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=933 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Ambari组件部署布局';

/*Table structure for table `ambari_config_item` */

CREATE TABLE `ambari_config_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键;自增主键',
  `stack_code` varchar(64) NOT NULL COMMENT 'Stack的Code;如：SDP-1.0.0',
  `service_code` varchar(64) NOT NULL COMMENT '大数据服务Code;如：HADOOP',
  `component_code` varchar(64) NOT NULL COMMENT '大数据组件Code;如：NAMENODE',
  `config_type_code` varchar(64) NOT NULL COMMENT '配置项代码;如:core-site',
  `key` varchar(1024) NOT NULL COMMENT '配置项名',
  `value` mediumtext COMMENT '配置项值',
  `is_content_prop` tinyint NOT NULL COMMENT '是否是内容字段',
  `is_dynamic` tinyint NOT NULL COMMENT '是否动态计算的配置字段',
  `dynamic_type` varchar(64) DEFAULT NULL COMMENT '动态配置类型',
  `item_type` varchar(255) DEFAULT NULL COMMENT '配置类型;如：',
  `state` varchar(16) NOT NULL DEFAULT 'VALID' COMMENT '状态;VALID，INVALID，DELETED',
  `created_by` varchar(32) DEFAULT NULL COMMENT '创建人;创建人',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间;创建时间',
  `updated_by` varchar(32) DEFAULT NULL COMMENT '更新人;更新人',
  `updated_time` datetime DEFAULT NULL COMMENT '更新时间;更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_service_component_config_type` (`service_code`,`component_code`),
  KEY `idx_config_type_code` (`config_type_code`)
) ENGINE=InnoDB AUTO_INCREMENT=3488 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Ambari默认配置项';

/*Table structure for table `ambari_config_item_attr` */

CREATE TABLE `ambari_config_item_attr` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键;自增主键',
  `stack_code` varchar(64) NOT NULL COMMENT 'Stack的Code;如：SDP-1.0.0',
  `service_code` varchar(64) NOT NULL COMMENT '大数据服务Code;如：HADOOP',
  `component_code` varchar(64) NOT NULL COMMENT '大数据组件Code;如：NAMENODE',
  `config_type_code` varchar(64) NOT NULL COMMENT '配置项代码;如:core-site',
  `tag_name` varchar(64) NOT NULL COMMENT '标识;生成配置的对象Tag名，如：final',
  `key` varchar(1024) NOT NULL COMMENT '配置项名;配置项名称',
  `value` varchar(2048) DEFAULT NULL COMMENT '配置项值;配置项值',
  `state` varchar(16) NOT NULL DEFAULT 'VALID' COMMENT '状态;VALID，INVALID，DELETED',
  `created_by` varchar(32) DEFAULT NULL COMMENT '创建人;创建人',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间;创建时间',
  `updated_by` varchar(32) DEFAULT NULL COMMENT '更新人;更新人',
  `updated_time` datetime DEFAULT NULL COMMENT '更新时间;更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_service_component_config_type` (`service_code`,`component_code`),
  KEY `idx_config_type_code` (`config_type_code`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Ambari配置项属性，默认在Stack中的配置';

/*Table structure for table `ambari_config_type` */

CREATE TABLE `ambari_config_type` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键;自增主键',
  `stack_code` varchar(64) NOT NULL COMMENT 'Stack的代码;如：SDP-1.0.0',
  `service_code` varchar(64) NOT NULL COMMENT '大数据服务ID;如：HADOOP',
  `component_code` varchar(64) NOT NULL COMMENT '大数据组件ID;如：NAMENODE',
  `code` varchar(32) NOT NULL COMMENT '配置代码;如：core-site',
  `name` varchar(64) NOT NULL COMMENT '配置名称;如：HDFS设置环境变量脚本',
  `file_type` varchar(32) NOT NULL COMMENT '文件类型;SHELL, PROPERTIES, YAML, XML',
  `is_file_content` tinyint NOT NULL COMMENT '是否是配置文件内容;1:是  0：不是',
  `mapping_code` varchar(32) DEFAULT NULL COMMENT '映射代码;Shein对应的代码为 core-site',
  `created_by` varchar(32) DEFAULT NULL COMMENT '创建人;创建人',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间;创建时间',
  `updated_by` varchar(32) DEFAULT NULL COMMENT '更新人;更新人',
  `updated_time` datetime DEFAULT NULL COMMENT '更新时间;更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Ambari配置类型';

/*Table structure for table `base_cluster_operation_template` */

CREATE TABLE `base_cluster_operation_template` (
  `template_id` varchar(40) NOT NULL COMMENT '模版ID',
  `release_version` varchar(255) DEFAULT NULL COMMENT '发布版本',
  `operation_name` varchar(20) DEFAULT NULL COMMENT '操作类型;创建、释放，扩缩容等。',
  `operation_description` varchar(255) DEFAULT NULL COMMENT '操作描述',
  `is_delete` int DEFAULT NULL COMMENT '是否删除;0 无效 1 有效',
  `createdby` varchar(60) DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='集群操作计划模版;-';

/*Table structure for table `base_cluster_operation_template_activity` */

CREATE TABLE `base_cluster_operation_template_activity` (
  `activity_id` varchar(40) NOT NULL COMMENT '活动ID',
  `template_id` varchar(40) DEFAULT NULL COMMENT '模版ID',
  `activity_type` varchar(100) DEFAULT NULL COMMENT '活动类型',
  `activity_cnname` varchar(60) DEFAULT NULL COMMENT '活动类型中文名称',
  `activity_name` varchar(100) DEFAULT NULL COMMENT '活动名称',
  `sort_no` int DEFAULT NULL COMMENT '执行顺序;按升序排序',
  `timeout` int DEFAULT NULL COMMENT '超时时间（秒）',
  `createdby` varchar(60) DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='集群操作计划活动明细模版';

/*Table structure for table `base_cluster_script` */

CREATE TABLE `base_cluster_script` (
  `conf_script_id` varchar(40) NOT NULL COMMENT '脚本ID',
  `release_version` varchar(40) DEFAULT NULL COMMENT '发行版本号',
  `script_name` varchar(60) DEFAULT NULL COMMENT '脚本名称',
  `run_timing` varchar(30) DEFAULT NULL COMMENT '执行时机;aftervminit,beforestart,afterstart',
  `playbook_uri` varchar(255) DEFAULT NULL COMMENT 'playbook脚本',
  `script_file_uri` varchar(300) DEFAULT NULL COMMENT '脚本路径',
  `extra_vars` varchar(300) DEFAULT NULL COMMENT '扩展',
  `sort_no` int DEFAULT NULL COMMENT '脚本执行顺序;升序',
  `createdby` varchar(60) DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`conf_script_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='集群预置脚本列表';

/*Table structure for table `base_dictionary` */

CREATE TABLE `base_dictionary` (
  `dict_id` int NOT NULL AUTO_INCREMENT COMMENT '字典ID',
  `pdict_id` varchar(255) DEFAULT NULL COMMENT '父节点ID',
  `dict_name` varchar(60) DEFAULT NULL COMMENT '字典名称',
  `dict_value` varchar(300) DEFAULT NULL COMMENT '字典值',
  `alias_name` varchar(255) DEFAULT NULL COMMENT '字典别名;别名为英文，且唯一。',
  `is_delete` varchar(255) DEFAULT NULL COMMENT '是否删除',
  `sortno` varchar(255) DEFAULT NULL COMMENT '排序',
  `createdby` varchar(60) DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  `modifiedby` varchar(60) DEFAULT NULL COMMENT '修改人',
  `modified_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`dict_id`)
) ENGINE=InnoDB AUTO_INCREMENT=53 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='字典表';

/*Table structure for table `base_release_apps` */

CREATE TABLE `base_release_apps` (
  `release_version` varchar(32) NOT NULL COMMENT '发行版本号',
  `app_name` varchar(32) NOT NULL COMMENT '应用组件名称',
  `app_verison` varchar(20) DEFAULT NULL COMMENT '应用组件版本',
  `required` int DEFAULT NULL COMMENT '是否必选',
  `sort_no` int DEFAULT NULL COMMENT '显示排序',
  `created_by` varchar(60) DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`release_version`,`app_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='发行版包含应用组件';

/*Table structure for table `base_release_apps_config` */

CREATE TABLE `base_release_apps_config` (
  `release_version` varchar(40) NOT NULL COMMENT '发行版本号',
  `app_name` varchar(40) NOT NULL COMMENT '组件名称',
  `app_config_classification` varchar(20) NOT NULL COMMENT '配置分类',
  `app_config_file` varchar(100) DEFAULT NULL COMMENT '配置文件',
  `sort_no` int DEFAULT NULL COMMENT '显示排序',
  `is_delete` int DEFAULT NULL COMMENT '是否删除;0 无效 1 有效',
  `createdby` varchar(60) DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`release_version`,`app_config_classification`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='发行版本可用应用组件配置列表; sum(各个应用配置项数量）< 200';

/*Table structure for table `base_release_version` */

CREATE TABLE `base_release_version` (
  `release_version` varchar(32) NOT NULL COMMENT '发行版本号',
  `release_description` varchar(300) DEFAULT NULL COMMENT '版本描述',
  `create_by` varchar(60) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`release_version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='集群发行版本';

/*Table structure for table `base_release_vm_img` */

CREATE TABLE `base_release_vm_img` (
  `release_version` varchar(40) NOT NULL COMMENT '发行版本号',
  `vm_role` varchar(40) NOT NULL COMMENT '实例角色;ambari master core task',
  `os_imageid` varchar(300) DEFAULT NULL COMMENT '镜像ID',
  `os_image_type` varchar(20) DEFAULT NULL COMMENT '镜像类型;标准/自定义',
  `os_version` varchar(60) DEFAULT NULL COMMENT '镜像内系统版本号',
  `createdby` varchar(60) DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`release_version`,`vm_role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='发行版本使用操作系统; sum(各个应用配置项数量）< 200';

/*Table structure for table `base_scene` */

CREATE TABLE `base_scene` (
  `scene_id` varchar(50) NOT NULL COMMENT '场景ID',
  `cluster_release_ver` varchar(20)  DEFAULT NULL COMMENT '集群版本号',
  `scene_name` varchar(100)  DEFAULT NULL COMMENT '场景名称',
  `scene_desc` varchar(300)  DEFAULT NULL COMMENT '场景描述',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  `createdby` varchar(32)  DEFAULT NULL COMMENT '创建人',
  PRIMARY KEY (`scene_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='场景基础信息;-';

/*Table structure for table `base_scene_apps` */

CREATE TABLE `base_scene_apps` (
  `scene_id` varchar(50)  NOT NULL COMMENT '场景ID',
  `app_name` varchar(100)  NOT NULL COMMENT '组件名称',
  `app_version` varchar(30)  DEFAULT NULL COMMENT '组件版本号',
  `required` int DEFAULT '0' COMMENT '是否必选;1：必选  0：不必选',
  `sort_no` int DEFAULT NULL COMMENT '排序序号',
  PRIMARY KEY (`scene_id`,`app_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='场景应用组件基础信息; -';

/*Table structure for table `base_script` */

CREATE TABLE `base_script` (
  `script_id` varchar(40)  NOT NULL COMMENT '脚本ID（UUID）',
  `script_name` varchar(255)  NOT NULL COMMENT '脚本名',
  `blob_path` varchar(255)  DEFAULT NULL COMMENT '脚本在blob上的存储路径',
  `upload_time` datetime DEFAULT NULL COMMENT '脚本上传时间',
  `remark` varchar(1024)  DEFAULT NULL COMMENT '脚本备注',
  `state` varchar(32)  NOT NULL DEFAULT 'VALID' COMMENT '脚本状态;VALID，INVALID，DELETED',
  PRIMARY KEY (`script_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户上传的需要执行的脚本库';

/*Table structure for table `base_user_info` */

CREATE TABLE `base_user_info` (
  `user_id` varchar(60)  NOT NULL COMMENT '用户ID',
  `user_name` varchar(60) NOT NULL COMMENT '账号',
  `real_name` varchar(60)  DEFAULT NULL COMMENT '用户姓名',
  `dept_id` varchar(200)  DEFAULT NULL COMMENT '部门ID',
  `dept_name` varchar(200)  DEFAULT NULL COMMENT '部门名称',
  `password` varchar(60)  NOT NULL COMMENT '密码',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  `createdby` varchar(60)  DEFAULT NULL COMMENT '创建人',
  `modified_time` datetime DEFAULT NULL COMMENT '修改时间',
  `modifiedby` varchar(60)  DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `user_name` (`user_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='集群用户信息表';

/*Table structure for table `conf_cluster` */

CREATE TABLE `conf_cluster` (
  `cluster_id` varchar(40) NOT NULL COMMENT '集群ID',
  `cluster_name` varchar(200) DEFAULT NULL COMMENT '集群名称',
  `cluster_release_ver` varchar(20) DEFAULT NULL COMMENT '集群版本号',
  `region` varchar(30) DEFAULT NULL COMMENT '区域/数据中心',
  `scene` varchar(60) DEFAULT NULL COMMENT '场景',
  `zone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '可用区代码',
  `vnet` varchar(60) DEFAULT NULL COMMENT '虚拟网/VPC',
  `subnet` varchar(300) DEFAULT NULL COMMENT '子网',
  `config_type` varchar(30) DEFAULT NULL COMMENT '集群组件配置模式;分类/自定义',
  `master_security_group` varchar(300) DEFAULT NULL COMMENT '主安全组(master节点使用)',
  `slave_security_group` varchar(300) DEFAULT NULL COMMENT '从安全组(core/task节点使用）',
  `log_path` varchar(200) DEFAULT NULL COMMENT '日志的对象存储路径',
  `log_mi` varchar(256) DEFAULT NULL COMMENT '日志桶托管标识',
  `keypair_id` varchar(200) DEFAULT NULL COMMENT '密钥对ID',
  `vm_mi` varchar(256) DEFAULT NULL COMMENT '虚拟机托管标识',
  `vm_mi_tenant_id` varchar(64) DEFAULT NULL COMMENT '集群节点托管MI的TenantId',
  `vm_mi_client_id` varchar(64) DEFAULT NULL COMMENT '集群节点托管MI的ClientId',
  `delete_protected` varchar(1) DEFAULT NULL COMMENT '关闭保护',
  `instance_collection_type` varchar(20) DEFAULT NULL COMMENT '实例组织类型;group 实例组 / queue 实例队列',
  `publicIp_available` varchar(1) DEFAULT NULL COMMENT '公网IP是否可用',
  `ambari_dburl` varchar(300) DEFAULT NULL COMMENT 'ambari数据库地址',
  `ambari_port` varchar(20) DEFAULT NULL COMMENT 'ambari数据库服务器端口',
  `ambari_database` varchar(50) DEFAULT NULL COMMENT 'ambari数据库库名',
  `ambari_db_autocreate` int DEFAULT NULL COMMENT '是否自动创建ambari数据库',
  `hive_metadata_dburl` varchar(300) DEFAULT NULL COMMENT 'hive元数据数据库地址',
  `hive_metadata_port` varchar(20) DEFAULT NULL COMMENT 'hive元数据库服务器端口',
  `hive_metadata_database` varchar(50) DEFAULT NULL COMMENT 'hive元数据库库名',
  `ambari_acount` varchar(255) DEFAULT NULL COMMENT 'ambari账号',
  `src_cluster_id` varchar(40) DEFAULT NULL COMMENT '复制源集群ID',
  `is_ha` int DEFAULT NULL COMMENT '是否高可用',
  `state` int DEFAULT NULL COMMENT '状态;0 待创建 1 创建中 2 已创建  -1释放中 -2 已释放',
  `createdby` varchar(60) DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  `modifiedby` varchar(60) DEFAULT NULL COMMENT '修改人',
  `modified_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`cluster_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='集群配置; 1';

/*Table structure for table `conf_cluster_app` */

CREATE TABLE `conf_cluster_app` (
  `cluster_id` varchar(40) NOT NULL COMMENT '集群ID',
  `app_name` varchar(40) NOT NULL COMMENT '组件名称',
  `app_version` varchar(30) DEFAULT NULL COMMENT '组件版本',
  `createdby` varchar(60) DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`cluster_id`,`app_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='集群部署应用; 1*集群要部署应用数量';

/*Table structure for table `conf_cluster_apps_config` */

CREATE TABLE `conf_cluster_apps_config` (
  `app_config_item_id` varchar(255) NOT NULL COMMENT '组件配置项',
  `cluster_id` varchar(40) NOT NULL COMMENT '集群ID',
  `app_name` varchar(40) DEFAULT NULL COMMENT '组件名称',
  `app_config_classification` varchar(20) DEFAULT NULL COMMENT '配置分类',
  `config_item` varchar(256) DEFAULT NULL COMMENT '配置项',
  `config_val` varchar(256) DEFAULT NULL COMMENT '配置项value',
  `is_delete` int DEFAULT NULL COMMENT '是否删除;0 无效 1 有效',
  `createdby` varchar(60) DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`app_config_item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='集群应用组件配置; sum(各个应用配置项数量）< 200';

/*Table structure for table `conf_cluster_script` */

CREATE TABLE `conf_cluster_script` (
  `conf_script_id` varchar(40) DEFAULT NULL COMMENT '脚本ID',
  `cluster_id` varchar(40) DEFAULT NULL COMMENT '集群ID',
  `script_name` varchar(60) DEFAULT NULL COMMENT '脚本名称',
  `run_timing` varchar(30) DEFAULT NULL COMMENT '执行时机;aftervminit,beforestart,afterstart',
  `script_path` varchar(300) DEFAULT NULL COMMENT '脚本路径',
  `script_param` text NULL COMMENT '脚本参数',
  `sort_no` int DEFAULT NULL COMMENT '排序',
  `createdby` varchar(60) DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  `node_list` text COMMENT '执行脚本的机器列表'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='集群可执行脚本列表';

/*Table structure for table `conf_cluster_tag` */

CREATE TABLE `conf_cluster_tag` (
  `cluster_id` varchar(40) NOT NULL COMMENT '集群ID',
  `tag_group` varchar(300) NOT NULL COMMENT '标签组',
  `tag_val` text COMMENT '标签值',
  PRIMARY KEY (`cluster_id`,`tag_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='集群标签表; 1*标签数量';

/*Table structure for table `conf_cluster_vm` */

CREATE TABLE `conf_cluster_vm` (
  `vm_conf_id` varchar(40) NOT NULL COMMENT '实例配置ID',
  `cluster_id` varchar(40) DEFAULT NULL COMMENT '集群ID',
  `vm_role` varchar(30) DEFAULT NULL COMMENT '实例角色;Master Core Task',
  `group_name` varchar(60) DEFAULT NULL COMMENT '实例组名称',
  `elastic_rule_id` varchar(40) DEFAULT NULL COMMENT '弹性伸缩规则ID',
  `sku` varchar(50) DEFAULT NULL COMMENT '实例规格',
  `os_imageid` varchar(300) DEFAULT NULL COMMENT '镜像ID',
  `os_image_type` varchar(20) DEFAULT NULL COMMENT '镜像类型;标准/自定义',
  `os_version` varchar(60) DEFAULT NULL COMMENT '镜像内系统版本号',
  `os_volume_size` int DEFAULT NULL COMMENT 'OS磁盘大小',
  `os_volume_type` varchar(30) DEFAULT NULL COMMENT 'OS磁盘类型',
  `vcpus` varchar(255) DEFAULT NULL COMMENT 'CPU核数',
  `memory` varchar(255) DEFAULT NULL COMMENT '内存大小（GB）',
  `count` int DEFAULT NULL COMMENT '实例购买数量',
  `purchase_type` int DEFAULT NULL COMMENT '购买类型;1 按需  2 竞价',
  `init_script_path` varchar(1000) DEFAULT NULL COMMENT '初始化脚本地址',
  `state` int DEFAULT NULL COMMENT '状态;0 待创建 1 创建中 2 已创建  -1释放中 -2 已释放',
  `createdby` varchar(60) DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  `modifiedby` varchar(60) DEFAULT NULL COMMENT '修改人',
  `modified_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`vm_conf_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='集群实例组模式节点配置; 1000';

/*Table structure for table `conf_cluster_vm_data_volume` */

CREATE TABLE `conf_cluster_vm_data_volume` (
  `volume_conf_id` varchar(40) NOT NULL COMMENT '磁盘配置ID',
  `vm_conf_id` varchar(40) DEFAULT NULL COMMENT '实例配置ID',
  `data_volume_type` varchar(30) DEFAULT NULL COMMENT '数据盘类型',
  `local_volume_type` varchar(30) DEFAULT NULL COMMENT '本地数据盘类型',
  `data_volume_size` int DEFAULT NULL COMMENT '数据盘大小（GB）',
  `count` int DEFAULT NULL COMMENT '购买磁盘数量',
  PRIMARY KEY (`volume_conf_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='集群实例数据盘配置; 1000';

/*Table structure for table `conf_keypair` */

CREATE TABLE `conf_keypair` (
  `keypair_id` varchar(200) NOT NULL COMMENT '密钥对ID',
  `keypair_name` varchar(300) DEFAULT NULL COMMENT '密钥对名称',
  `keypair_type` varchar(20) DEFAULT NULL COMMENT '密钥对加密类型;RSA/ED25519',
  `public_key` text COMMENT '公钥',
  `private_key` text COMMENT '私钥',
  `createdby` varchar(60) DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='密钥配置; <=1';

/*Table structure for table `conf_tag_keys` */

CREATE TABLE `conf_tag_keys` (
  `tag_key` varchar(20) DEFAULT NULL COMMENT '标签名称',
  `createdby` varchar(60) DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='标签字典库';

/*Table structure for table `config_core` */

CREATE TABLE `config_core` (
  `id` int NOT NULL AUTO_INCREMENT,
  `akey` varchar(150) DEFAULT NULL,
  `avalue` varchar(1500) DEFAULT NULL,
  `application` varchar(150) DEFAULT NULL,
  `profile` varchar(150) DEFAULT NULL,
  `label` varchar(150) DEFAULT NULL,
  `mwtype` varchar(60) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1436 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

/*Table structure for table `config_detail` */

CREATE TABLE `config_detail` (
  `id` int NOT NULL AUTO_INCREMENT,
  `akey` varchar(200) NOT NULL,
  `avalue` longtext NOT NULL,
  `application` varchar(50) NOT NULL COMMENT '应用名称',
  `profile` varchar(50) NOT NULL COMMENT '应用模块',
  `label` varchar(50) NOT NULL COMMENT '应用环境',
  `mwtype` varchar(20) DEFAULT NULL COMMENT '中间件类型',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4185 DEFAULT CHARSET=utf8mb4;

/*Table structure for table `info_cluster` */

CREATE TABLE `info_cluster` (
  `cluster_id` varchar(40) NOT NULL COMMENT '集群ID',
  `ambari_username` varchar(60) DEFAULT NULL COMMENT 'ambari用户名',
  `ambari_password` varchar(100) DEFAULT NULL COMMENT 'ambari密码;对称加密存储',
  `ambari_host` varchar(100) DEFAULT NULL COMMENT 'ambari地址',
  `master_ips` varchar(200) DEFAULT NULL,
  `ambari_count` int DEFAULT NULL COMMENT 'ambari实例数量',
  `master_vms_count` int DEFAULT NULL COMMENT 'master实例数量',
  `core_vms_count` int DEFAULT NULL COMMENT 'core实例数量',
  `task_vms_count` int DEFAULT NULL COMMENT 'task实例数量',
  `apps_count` int DEFAULT NULL COMMENT '组件应用数量',
  `cluster_create_begtime` datetime DEFAULT NULL COMMENT '集群创建开始时间',
  `cluster_create_endtime` datetime DEFAULT NULL COMMENT '集群创建完成时间',
  PRIMARY KEY (`cluster_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='集群创建信息; 1';

/*Table structure for table `info_cluster_operation_plan` */

CREATE TABLE `info_cluster_operation_plan` (
  `plan_id` varchar(40) NOT NULL COMMENT '计划ID',
  `cluster_id` varchar(40) DEFAULT NULL COMMENT '集群ID',
  `template_id` varchar(40) DEFAULT NULL COMMENT '模版ID',
  `operation_type` varchar(20) DEFAULT NULL COMMENT '操作类型',
  `beg_time` datetime DEFAULT NULL COMMENT '计划任务开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '计划任务结束时间',
  `createdby` varchar(60) DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`plan_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='集群操作计划; 1';

/*Table structure for table `info_cluster_operation_plan_activity_log` */

CREATE TABLE `info_cluster_operation_plan_activity_log` (
  `activity_log_id` varchar(40) NOT NULL COMMENT '执行计划操作步骤ID',
  `plan_id` varchar(40) DEFAULT NULL COMMENT '操作计划ID',
  `activity_id` varchar(40) DEFAULT NULL COMMENT '活动ID',
  `template_id` varchar(40) DEFAULT NULL COMMENT '模版ID',
  `activity_type` varchar(100) DEFAULT NULL COMMENT '活动类型',
  `activity_cnname` varchar(60) DEFAULT NULL,
  `activity_name` varchar(100) DEFAULT NULL COMMENT '活动名称',
  `sort_no` int DEFAULT NULL COMMENT '执行顺序;按升序排序',
  `timeout` int DEFAULT NULL,
  `begtime` datetime DEFAULT NULL COMMENT '开始执行时间',
  `endtime` datetime DEFAULT NULL COMMENT '完成时间',
  `duration` int DEFAULT NULL COMMENT '持续时间（秒）',
  `paraminfo` text COMMENT '参数信息',
  `logs` text COMMENT '执行过程日志',
  `state` int DEFAULT NULL COMMENT '执行状态;0 未执行 1 执行中 2 执行完成  -1 执行超时  -2 执行失败',
  `createdby` varchar(60) DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`activity_log_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='集群操作计划活动明细';

/*Table structure for table `info_cluster_playbook_job` */

CREATE TABLE `info_cluster_playbook_job` (
  `transaction_id` varchar(40) NOT NULL COMMENT '操作流水',
  `cluster_id` varchar(40) DEFAULT NULL COMMENT '集群ID',
  `cluster_name` varchar(200) DEFAULT NULL COMMENT '集群名称',
  `activity_log_id` varchar(40) DEFAULT NULL COMMENT '调度任务活动ID',
  `job_id` varchar(40) DEFAULT NULL COMMENT '任务ID',
  `node_list` text COMMENT '节点IP',
  `playbook_uri` varchar(300) DEFAULT NULL COMMENT 'playbook 地址',
  `script_file_uri` text COMMENT '脚本文件地址',
  `extra_vars` varchar(255) DEFAULT NULL COMMENT '扩展参数',
  `sort_no` decimal(24,6) DEFAULT NULL COMMENT '执行顺序;升序',
  `job_status` int DEFAULT NULL COMMENT '任务状态;0 job创建完成 1 执行中 2 执行完成 3 job行失败',
  `beg_time` datetime DEFAULT NULL COMMENT '任务开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '任务结束时间',
  `conf_script_id` varchar(40) DEFAULT NULL COMMENT '脚本ID',
  `job_type` varchar(20) DEFAULT NULL COMMENT '脚本类型：sys 系统类型 user 用户自定义脚本',
  PRIMARY KEY (`transaction_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='playbook任务信息';

/*Table structure for table `info_cluster_vm` */

CREATE TABLE `info_cluster_vm` (
  `cluster_id` varchar(40) NOT NULL COMMENT '集群ID',
  `vm_name` varchar(60) NOT NULL COMMENT '实例名称',
  `vm_conf_id` varchar(40) DEFAULT NULL COMMENT '实例配置ID',
  `host_name` varchar(200) DEFAULT NULL COMMENT '机器名称',
  `internalIp` varchar(50) DEFAULT NULL COMMENT '内网IP',
  `default_username` varchar(20) DEFAULT NULL COMMENT '默认用户名',
  `vm_role` varchar(45) DEFAULT NULL COMMENT '实例角色',
  `group_name` varchar(60) DEFAULT NULL COMMENT '实例组名称',
  `sku_name` varchar(255) DEFAULT NULL COMMENT 'sku名称',
  `purchase_type` varchar(255) DEFAULT NULL COMMENT '购买类型',
  `imageid` varchar(300) DEFAULT NULL COMMENT '镜像ID',
  `create_transcation_id` varchar(50) DEFAULT NULL COMMENT '创建虚拟机流水号',
  `create_job_id` varchar(200) DEFAULT NULL COMMENT '创建虚拟机的任务id',
  `create_begtime` datetime DEFAULT NULL COMMENT '虚拟机创建开始时间',
  `create_endtime` datetime DEFAULT NULL COMMENT '虚拟机创建完成时间',
  `state` int DEFAULT NULL COMMENT 'vm 运行状态， 0 停止 1 运行中 -1 已销毁',
  PRIMARY KEY (`cluster_id`,`vm_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='集群实例创建信息; 1000';

/*Table structure for table `info_cluster_vm_job` */

CREATE TABLE `info_cluster_vm_job` (
  `transaction_id` varchar(40) NOT NULL COMMENT '操作流水',
  `cluster_id` varchar(40) DEFAULT NULL COMMENT '集群ID',
  `cluster_name` varchar(200) DEFAULT NULL COMMENT '集群名称',
  `operation_type` varchar(20) DEFAULT NULL COMMENT '操作类型;create/delete',
  `activity_log_id` varchar(40) DEFAULT NULL COMMENT '调度任务活动ID',
  `job_id` varchar(200) DEFAULT NULL COMMENT '任务ID',
  `job_status` int DEFAULT NULL COMMENT '任务状态;0 job创建完成 1 执行中 2 执行完成 3 job行失败',
  `beg_time` datetime DEFAULT NULL COMMENT '任务开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '任务结束时间',
  PRIMARY KEY (`transaction_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='操作虚拟机任务信息';

/*Table structure for table `stack_service_component` */

CREATE TABLE `stack_service_component` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键;自增主键',
  `stack_code` varchar(64) NOT NULL COMMENT 'Stack的代码;如：SDP-1.0.0',
  `service_code` varchar(64) NOT NULL COMMENT '服务代码;如：HADOOP',
  `code` varchar(32) NOT NULL COMMENT '组件代码;如：NAMENODE',
  `name` varchar(64) NOT NULL COMMENT '组件名称;如：NameNode进程',
  `default_path` varchar(512) DEFAULT NULL COMMENT '组件默认安装目录;如：/usr/local/hadoop',
  `mapping_code` varchar(32) DEFAULT NULL COMMENT '映射外部系统的组件代码;如：NAMENODE',
  `is_client` tinyint DEFAULT '0' COMMENT '是否是客户端;1：客户端组件 0：服务端组件',
  `created_by` varchar(32) DEFAULT NULL COMMENT '创建人;创建人',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间;创建时间',
  `updated_by` varchar(32) DEFAULT NULL COMMENT '更新人;更新人',
  `updated_time` datetime DEFAULT NULL COMMENT '更新时间;更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Stack布署的大数据组件';
