-- 补齐驱逐主机Template
REPLACE INTO base_cluster_operation_template (template_id,release_version,operation_name,operation_description,is_delete,createdby,created_time) VALUES
    ('0586af02-9f50-4231-ad56-99219fc28780','SDP-1.0','scaleoutEvictVm','补齐驱逐主机',0,'system',now()),
    ('0e9ca7c1-baa1-4a82-9370-c29115772c81','SDP-2.0','scaleoutEvictVm','补齐驱逐主机',0,'system',now());

-- 补齐驱逐主机Template 活动步骤
INSERT INTO base_cluster_operation_template_activity (activity_id,template_id,activity_type,activity_cnname,activity_name,sort_no,timeout,createdby,created_time) VALUES
    ('5fdf49c5-1c16-45a2-8d26-5e686edf77dd','0586af02-9f50-4231-ad56-99219fc28780','azureVMService','初始化驱逐主机','initEvictVms',0,0,'system',NULL),
    ('7ce22ece-81c2-415c-9694-ab5a932affd2','0586af02-9f50-4231-ad56-99219fc28780','InstallSDP','执行用户自定义脚本','initScript',1,0,'system',NULL),
    ('7fe71f40-68a2-4ca4-9247-800e3f4e559c','0586af02-9f50-4231-ad56-99219fc28780','InstallSDP','查询用户自定义脚本进展','queryPlayJobStatus',2,0,'system',NULL),
    ('6640da48-8263-4480-b9b7-2b6568a55884','0586af02-9f50-4231-ad56-99219fc28780','InstallSDP','执行安装ambari-agent','installAgent',3,0,'system',NULL),
    ('98bcf9f3-1a0c-45b7-88d7-8e7eb24ee9e6','0586af02-9f50-4231-ad56-99219fc28780','InstallSDP','查询安装ambari-agent进度','queryPlayJobStatus',4,0,'system',NULL),
    ('82cd710f-5a8b-450f-b42a-0d070c0083b2','0586af02-9f50-4231-ad56-99219fc28780','clusterservice','ambari-server添加实例','ambariAddHosts',5,0,'system',NULL),
    ('3c2471a3-9265-4d1f-b9c5-bfef26fab372','0586af02-9f50-4231-ad56-99219fc28780','clusterservice','将实例添加到ambari配置组中','ambariAddHostsToConfigGroup',6,0,'system',NULL),
    ('9ca5b571-a38e-4a39-8fed-0c8e3f551980','0586af02-9f50-4231-ad56-99219fc28780','clusterservice','ambari-server新增实例配置需要安装的服务','configAddSDP',7,0,'system',NULL),
    ('f4699ad6-4191-4f0a-bc18-fba82792387c','0586af02-9f50-4231-ad56-99219fc28780','clusterservice','ambari-server新增实例安装服务','installAddSDP',8,0,'system',NULL);
INSERT INTO base_cluster_operation_template_activity (activity_id,template_id,activity_type,activity_cnname,activity_name,sort_no,timeout,createdby,created_time) VALUES
    ('3433fa8c-786b-4053-b45a-ad924201069f','0586af02-9f50-4231-ad56-99219fc28780','clusterservice','查询ambari-server新增实例安装服务进展','querySDPClusterInstallProcess',9,0,'system',NULL),
    ('5c8016d1-2885-4dba-bcfc-8c699197d6e2','0586af02-9f50-4231-ad56-99219fc28780','InstallSDP','执行集群启动前脚本','beforeClusterStartScript',10,0,'system',NULL),
    ('b2b2daad-971c-4aef-bb2d-55e79bae81bc','0586af02-9f50-4231-ad56-99219fc28780','InstallSDP','查询启动前脚本进展','queryPlayJobStatus',11,0,'system',NULL),
    ('c5cab2b6-b734-4614-b71f-7d1f52dbbf15','0586af02-9f50-4231-ad56-99219fc28780','InstallSDP','执行集群启动后脚本','afterClusterCompletedScript',12,0,'system',NULL),
    ('7a5bac42-0c7e-42b7-97da-6a23a4443b4c','0586af02-9f50-4231-ad56-99219fc28780','InstallSDP','查询集群启动后脚本执行进展','queryPlayJobStatus',13,0,'system',NULL),
    ('329f7a3c-650f-4dfe-878c-bc48ce7c0632','0586af02-9f50-4231-ad56-99219fc28780','clusterservice','新增实例启动服务','startAddHostComponents',14,0,'system',NULL),
    ('fbe8ae6b-7c45-4ea8-b788-68e0be36e0c0','0586af02-9f50-4231-ad56-99219fc28780','clusterservice','查询新增实例启动服务进展','querySDPClusterInstallProcess',15,0,'system',NULL),
    ('8d7699db-2a3d-4452-be63-338e34feff4a','0586af02-9f50-4231-ad56-99219fc28780','clusterservice','做数据平衡（仅Core节点需要）','dataBalanceForCore',16,0,'system',NULL);


INSERT INTO base_cluster_operation_template_activity (activity_id,template_id,activity_type,activity_cnname,activity_name,sort_no,timeout,createdby,created_time) VALUES
    ('7510cdf2-b765-40ba-a713-d3bd59eedfc8','0e9ca7c1-baa1-4a82-9370-c29115772c81','azureVMService','初始化驱逐主机','initEvictVms',0,0,'system',NULL),
    ('3431b240-711c-43f4-afa4-556df3cdc33d','0e9ca7c1-baa1-4a82-9370-c29115772c81','InstallSDP','执行用户自定义脚本','initScript',1,0,'system',NULL),
    ('a10b553f-4df6-4728-b239-df437dd4a873','0e9ca7c1-baa1-4a82-9370-c29115772c81','InstallSDP','查询用户自定义脚本进展','queryPlayJobStatus',2,0,'system',NULL),
    ('61d6f0da-9f2d-44f9-b327-379fcfb99677','0e9ca7c1-baa1-4a82-9370-c29115772c81','InstallSDP','执行安装ambari-agent','installAgent',3,0,'system',NULL),
    ('b3d19bdb-5246-41a9-87e8-6371310189f6','0e9ca7c1-baa1-4a82-9370-c29115772c81','InstallSDP','查询安装ambari-agent进度','queryPlayJobStatus',4,0,'system',NULL),
    ('1035acf6-d0ad-4f24-bb47-3b12a760474a','0e9ca7c1-baa1-4a82-9370-c29115772c81','clusterservice','ambari-server添加实例','ambariAddHosts',5,0,'system',NULL),
    ('1c213446-ab39-4d14-8eb3-b09a71639ffa','0e9ca7c1-baa1-4a82-9370-c29115772c81','clusterservice','将实例添加到ambari配置组中','ambariAddHostsToConfigGroup',6,0,'system',NULL),
    ('035b67f8-bd06-4e84-86fd-1c4319b98f9b','0e9ca7c1-baa1-4a82-9370-c29115772c81','clusterservice','ambari-server新增实例配置需要安装的服务','configAddSDP',7,0,'system',NULL),
    ('300f2921-027a-4576-a502-5db50b3fbda5','0e9ca7c1-baa1-4a82-9370-c29115772c81','clusterservice','ambari-server新增实例安装服务','installAddSDP',8,0,'system',NULL);
INSERT INTO base_cluster_operation_template_activity (activity_id,template_id,activity_type,activity_cnname,activity_name,sort_no,timeout,createdby,created_time) VALUES
    ('75fcd005-e5c3-4d24-8393-89cef0fabb7f','0e9ca7c1-baa1-4a82-9370-c29115772c81','clusterservice','查询ambari-server新增实例安装服务进展','querySDPClusterInstallProcess',9,0,'system',NULL),
    ('43eb51d8-1f9a-4831-b420-5ec64eda7159','0e9ca7c1-baa1-4a82-9370-c29115772c81','InstallSDP','执行集群启动前脚本','beforeClusterStartScript',10,0,'system',NULL),
    ('8febab1f-ddad-47c4-a06f-08d514080b5c','0e9ca7c1-baa1-4a82-9370-c29115772c81','InstallSDP','查询启动前脚本进展','queryPlayJobStatus',11,0,'system',NULL),
    ('7d47a6c0-a9d5-4797-b547-b12f8bc559c9','0e9ca7c1-baa1-4a82-9370-c29115772c81','InstallSDP','执行集群启动后脚本','afterClusterCompletedScript',12,0,'system',NULL),
    ('85387f8d-7102-4559-a6b1-6480bbabaa0c','0e9ca7c1-baa1-4a82-9370-c29115772c81','InstallSDP','查询集群启动后脚本执行进展','queryPlayJobStatus',13,0,'system',NULL),
    ('fd1c5ea8-4729-4639-8470-75be2836b92e','0e9ca7c1-baa1-4a82-9370-c29115772c81','clusterservice','新增实例启动服务','startAddHostComponents',14,0,'system',NULL),
    ('94ccbeff-8afd-4c97-8083-2803b999958e','0e9ca7c1-baa1-4a82-9370-c29115772c81','clusterservice','查询新增实例启动服务进展','querySDPClusterInstallProcess',15,0,'system',NULL),
    ('e64c3338-3ab8-4e76-8512-d2010c55178c','0e9ca7c1-baa1-4a82-9370-c29115772c81','clusterservice','做数据平衡（仅Core节点需要）','dataBalanceForCore',16,0,'system',NULL);

-- 创建evict vm表
create table if not exists auto_created_evict_vms
(
    id             bigint  auto_increment NOT NULL COMMENT '自增ID' primary key,
    cluster_id     varchar(64)  not null comment '集群ID',
    cluster_name   varchar(128) null comment '集群名称',
    vm_role        varchar(48) null comment '主机角色',
    group_name     varchar(64) null comment '实例组名称',
    vm_name        varchar(64)     null comment '主机名',
    vmid           varchar(64)     null comment 'vmId',
    purchase_type  varchar(32)  null comment '购买类型: ',
    state          varchar(32)   not null comment '状态',
    event_content  text      not null comment '事件详情',
    create_time    datetime   not null comment '创建时间'
)  ENGINE=InnoDB comment '销毁集群配置表' DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ;

-- 增加索引
call create_index( 'auto_created_evict_vms', 'idx_create_time', '(create_time)' );
