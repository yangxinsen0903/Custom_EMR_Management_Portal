-- version表增加 SDP-2.0
REPLACE INTO base_release_version (release_version,release_description,create_by,create_time)
VALUES ('SDP-2.0','Hadoop组件版本升级','sysadmin',now());

-- SDP-2.0 流程模板与步骤
REPLACE INTO base_cluster_operation_template (template_id,release_version,operation_name,operation_description,is_delete,createdby,created_time) VALUES
      ('834f75e9-7227-11ed-85b7-6045bdc7fdc8','SDP-2.0','create','创建集群',0,'system',now()),
      ('834f75e9-7228-11ed-85b7-6045bdc7fdc8','SDP-2.0','delete','销毁集群',0,'system',now()),
      ('89a539c9-a615-11ed-922d-6045bdc792d8','SDP-2.0','restartservice','重启服务',0,'system',now()),
      ('84438518-9544-11ed-922d-6045bdc792d8','SDP-2.0','scalein','缩容集群',0,'system',now()),
      ('8fe546f4-9544-11ed-922d-6045bdc792d8','SDP-2.0','scaleout','扩容集群',0,'system',now()),
      ('8af19974-63ab-41a9-9512-1fb3fc22a2b8','SDP-2.0','clearvms','清理异常VM',0,'system',now()),
      ('80f52443-838b-11ed-8607-6045bdc792d8','SDP-2.0','runuserscript','执行用户脚本',0,'system',now()),
      ('84b3739a-a9d7-11ed-922d-6045bdc792d8','SDP-2.0','collectLogs','收集日志',0,'system',now()),
      ('89e0a922-adec-11ed-bcf2-6045bdc792d8','SDP-2.0','scaleoutpart','磁盘扩容',0,'system', now());
-- 创建集群
REPLACE INTO base_cluster_operation_template_activity (activity_id,template_id,activity_type,activity_cnname,activity_name,sort_no,timeout,createdby,created_time) VALUES
    ('d69c473b-74ad-11ed-85b7-6045bdc7fdcb','834f75e9-7227-11ed-85b7-6045bdc7fdc8','azureVMService','启动申请资源','createVms',-1,0,'system',NULL),
    ('34cfec84-74ae-11ed-85b7-6045bdc7fdcb','834f75e9-7227-11ed-85b7-6045bdc7fdc8','azureVMService','查询资源创建进展','queryVmsCreateJob',0,900,'system',NULL),
    ('e91af09a-78a3-11ed-85b7-6045bdc7fdcb','834f75e9-7227-11ed-85b7-6045bdc7fdc8','InstallSDP','执行初始化后客户脚本','initScript',1,0,'system',NULL),
    ('09d70ff1-78a4-11ed-85b7-6045bdc7fdcb','834f75e9-7227-11ed-85b7-6045bdc7fdc8','InstallSDP','查询初始化后客户脚本执行进展','queryPlayJobStatus',2,0,'system',NULL),
    ('330df4ed-7ac1-11ed-85b7-6045bdc7fdcb','834f75e9-7227-11ed-85b7-6045bdc7fdc8','clusterservice','安装前收集信息','sleep',3,900,'system',NULL),
    ('f02cb4bc-7861-11ed-85b7-6045bdc7fdcb','834f75e9-7227-11ed-85b7-6045bdc7fdc8','InstallSDP','启动部署Ambari-Server','installAmbari',4,0,'system',NULL),
    ('1b680f8e-7862-11ed-85b7-6045bdc7fdcb','834f75e9-7227-11ed-85b7-6045bdc7fdc8','InstallSDP','查询Ambari-Server部署进展','queryPlayJobStatus',5,0,'system',NULL),
    ('5b2528cf-789c-11ed-85b7-6045bdc7fdcb','834f75e9-7227-11ed-85b7-6045bdc7fdc8','InstallSDP','启动部署Ambari-Agent','installAgent',6,0,'system',NULL),
    ('935f8f37-789c-11ed-85b7-6045bdc7fdcb','834f75e9-7227-11ed-85b7-6045bdc7fdc8','InstallSDP','查询Ambari-Agent部署进展','queryPlayJobStatus',7,0,'system',NULL),
    ('5c4dbe92-78d0-11ed-85b7-6045bdc7fdcb','834f75e9-7227-11ed-85b7-6045bdc7fdc8','clusterservice','启动SDP部署','createSDPCluster',8,0,'system',NULL);
REPLACE INTO base_cluster_operation_template_activity (activity_id,template_id,activity_type,activity_cnname,activity_name,sort_no,timeout,createdby,created_time) VALUES
    ('5c4dc157-78d0-11ed-85b7-6045bdc7fdcb','834f75e9-7227-11ed-85b7-6045bdc7fdc8','clusterservice','查询SDP部署进展','querySDPClusterInstallProcess',9,3600,'system',NULL),
    ('05be6f4e-797a-11ed-85b7-6045bdc7fdcb','834f75e9-7227-11ed-85b7-6045bdc7fdc8','clusterservice','进行启动前准备','sleep',10,0,'system',NULL),
    ('876f4dbf-79f0-11ed-85b7-6045bdc7fdcb','834f75e9-7227-11ed-85b7-6045bdc7fdc8','InstallSDP','执行集群启动前客户脚本','beforeClusterStartScript',11,0,'system',NULL),
    ('876f527d-79f0-11ed-85b7-6045bdc7fdcb','834f75e9-7227-11ed-85b7-6045bdc7fdc8','InstallSDP','查询集群启动前客户脚本执行进展','queryPlayJobStatus',12,0,'system',NULL),
    ('5c4dc1dc-78d0-11ed-85b7-6045bdc7fdcb','834f75e9-7227-11ed-85b7-6045bdc7fdc8','clusterservice','启动集群应用','startSDPClusterApps',13,0,'system',NULL),
    ('876f5319-79f0-11ed-85b7-6045bdc7fdcb','834f75e9-7227-11ed-85b7-6045bdc7fdc8','clusterservice','查询集群应用启动进展','querySDPClusterInstallProcess',14,3600,'system',NULL),
    ('876f5379-79f0-11ed-85b7-6045bdc7fdcb','834f75e9-7227-11ed-85b7-6045bdc7fdc8','InstallSDP','执行集群启动后客户脚本','afterClusterCompletedScript',15,0,'system',NULL),
    ('876f53c9-79f0-11ed-85b7-6045bdc7fdcb','834f75e9-7227-11ed-85b7-6045bdc7fdc8','InstallSDP','查询集群启动后客户脚本执行进展','queryPlayJobStatus',16,0,'system',NULL),
    ('876f5369-79f0-11ed-85b7-6045bdc7fdcb','834f75e9-7227-11ed-85b7-6045bdc7fdc8','InstallSDP','启动安装TEZUI','installTezUI',17,0,'system',NULL),
    ('776f53c9-79f0-11ed-85b7-6045bdc7fdcb','834f75e9-7227-11ed-85b7-6045bdc7fdc8','InstallSDP','查询TEZUI安装执行进展','queryPlayJobStatus',18,0,'system',NULL);

-- 销毁集群
REPLACE INTO base_cluster_operation_template_activity (activity_id,template_id,activity_type,activity_cnname,activity_name,sort_no,timeout,createdby,created_time) VALUES
    ('f8f625f4-783c-11ed-85b7-6045bdc7fdcb','834f75e9-7228-11ed-85b7-6045bdc7fdc8','azureVMService','启动销毁集群','deleteVms',0,0,'system',NULL),
    ('02546cf9-783d-11ed-85b7-6045bdc7fdcb','834f75e9-7228-11ed-85b7-6045bdc7fdc8','azureVMService','查询销毁进展','queryDeleteVms',1,0,'system',NULL);

-- 重启服务
REPLACE INTO base_cluster_operation_template_activity (activity_id,template_id,activity_type,activity_cnname,activity_name,sort_no,timeout,createdby,created_time) VALUES
    ('c70a3244-a616-11ed-922d-6045bdc792db','89a539c9-a615-11ed-922d-6045bdc792d8','clusterservice','启动重启服务任务','restartSDPService',-1,0,'system',NULL),
    ('c70a3521-a616-11ed-922d-6045bdc792db','89a539c9-a615-11ed-922d-6045bdc792d8','clusterservice','查询重启服务结果','QuerySDPServiceRestartProcess',1,0,'system',NULL);

-- 缩容集群 34438518-9544-11ed-922d-6045bdc792d8
REPLACE INTO base_cluster_operation_template_activity (activity_id,template_id,activity_type,activity_cnname,activity_name,sort_no,timeout,createdby,created_time) VALUES
    ('370951c7-1156-41c8-8bf6-b66e4298983c','84438518-9544-11ed-922d-6045bdc792d8','InstallSDP','调整HDFS-BalanceBandWidth','scaleUpHdfsBalanceBandWidth',-1,0,'system',NULL),
    ('cb40ccdc-255c-4b3b-8d4e-90e9f0c38fec','84438518-9544-11ed-922d-6045bdc792d8','InstallSDP','查询调整HDFS-BalanceBandWidth结果','queryPlayJobStatus',0,0,'system',NULL),
    ('cd35e91f-9c00-11ed-922d-6045bdc792dc','84438518-9544-11ed-922d-6045bdc792d8','clusterservice','执行DataNode节点DECOMMISSION','dataNodeDecommionsion',1,0,'system',NULL),
    ('cd35eef5-9c00-11ed-922d-6045bdc792dc','84438518-9544-11ed-922d-6045bdc792d8','clusterservice','查询DataNode节点DECOMMISSION执行状态','queryDataNodeDecommission',2,0,'system',NULL),
    ('76fc44c0-b18a-4f73-929e-c1ad8c9c81fc','84438518-9544-11ed-922d-6045bdc792d8','InstallSDP','调整HDFS-BalanceBandWidth','scaleDownHdfsBalanceBandWidth',3,0,'system',NULL),
    ('10f6c5ba-de57-4293-9e72-b2ea30aab99c','84438518-9544-11ed-922d-6045bdc792d8','InstallSDP','查询调整HDFS-BalanceBandWidth结果','queryPlayJobStatus',4,0,'system',NULL),
    ('cd35f066-9c00-11ed-922d-6045bdc792dc','84438518-9544-11ed-922d-6045bdc792d8','clusterservice','执行NodeManger节点DECOMMISSION','nodeManagerDecommionsion',5,0,'system',NULL),
    ('cd35f152-9c00-11ed-922d-6045bdc792dc','84438518-9544-11ed-922d-6045bdc792d8','clusterservice','查询NodeManger节点DECOMMISSION执行状态','queryNodeManagerDecommission',6,0,'system',NULL),
    ('cd35f067-9c00-11ed-922d-6045bdc792dc','84438518-9544-11ed-922d-6045bdc792d8','clusterservice','执行HbaseRegionServer节点DECOMMISSION','hbaseRegionServerDecommionsion',7,0,'system',NULL),
    ('cd35f158-9c00-11ed-922d-6045bdc792dc','84438518-9544-11ed-922d-6045bdc792d8','clusterservice','查询HbaseRegionServer节点DECOMMISSION执行状态','queryHbaseRegionServerDecommission',8,0,'system',NULL);
REPLACE INTO base_cluster_operation_template_activity (activity_id,template_id,activity_type,activity_cnname,activity_name,sort_no,timeout,createdby,created_time) VALUES
    ('cd35f159-9c00-11ed-922d-6045bdc792dc','84438518-9544-11ed-922d-6045bdc792d8','clusterservice','优雅缩容等待','gracefullWating',9,2000,'system',NULL),
    ('cd35f233-9c00-11ed-922d-6045bdc792dc','84438518-9544-11ed-922d-6045bdc792d8','clusterservice','执行关闭组件命令','closeComponentByHost',10,0,'system',NULL),
    ('cd35f45b-9c00-11ed-922d-6045bdc792dc','84438518-9544-11ed-922d-6045bdc792d8','clusterservice','查询关闭组件命令状态','queryCloseComponentStatus',11,0,'system',NULL),
    ('cd35f53f-9c00-11ed-922d-6045bdc792dc','84438518-9544-11ed-922d-6045bdc792d8','clusterservice','ambari-server删除主机','deleteAmbariHosts',12,0,'system',NULL),
    ('cd35f54f-9c00-11ed-922d-6045bdc792dc','84438518-9544-11ed-922d-6045bdc792d8','clusterservice','ambari-server删除配置组','deleteAmbariHostGroup',13,0,'system','2023-02-27 12:03:29'),
    ('cd35f62b-9c00-11ed-922d-6045bdc792dc','84438518-9544-11ed-922d-6045bdc792d8','clusterservice','检查数据块 hdfs fsck （仅Core节点需要）','checkDataForCore',14,0,'system','2023-02-27 12:03:29'),
    ('cd35f6fe-9c00-11ed-922d-6045bdc792dc','84438518-9544-11ed-922d-6045bdc792d8','azureVMService','销毁主机','deleteVmsForScaleIn',15,0,'system','2023-02-27 12:03:29'),
    ('cd35f768-9c00-11ed-922d-6045bdc792dc','84438518-9544-11ed-922d-6045bdc792d8','azureVMService','查询销毁进展','queryScaleInVmsDeleteJob',16,0,'system','2023-02-27 12:03:29'),
    ('fd35f62b-9c00-11ed-922d-6045bdc792dc','84438518-9544-11ed-922d-6045bdc792d8','clusterservice','清理AmbariHost','clearAmbariHosts',17,0,'system','2023-02-22 15:39:27'),
    ('89e0318c-5a2e-49b5-b0e6-9fdcae64a7ac','84438518-9544-11ed-922d-6045bdc792d8','clusterservice','清理Ganglia数据','clearGangliaData',18,0,'system',NULL);
REPLACE INTO base_cluster_operation_template_activity (activity_id,template_id,activity_type,activity_cnname,activity_name,sort_no,timeout,createdby,created_time) VALUES
    ('c889e9cf-bb20-4ca3-ad51-a234a10a52bc','84438518-9544-11ed-922d-6045bdc792d8','InstallSDP','查询清理Ganglia数据进度','queryPlayJobStatus',19,0,'system',NULL);


-- 扩容集群
REPLACE INTO base_cluster_operation_template_activity (activity_id,template_id,activity_type,activity_cnname,activity_name,sort_no,timeout,createdby,created_time) VALUES
    ('e30964a9-954e-11ed-922d-6045bdc792dc','8fe546f4-9544-11ed-922d-6045bdc792d8','Scaling','申请服务器资源','createVms',-1,0,'system',NULL),
    ('e3096793-954e-11ed-922d-6045bdc792dc','8fe546f4-9544-11ed-922d-6045bdc792d8','azureVMService','查询扩容资源申请进展','queryVmsAppendJob',0,0,'system',NULL),
    ('e30967ff-954e-11ed-922d-6045bdc792dc','8fe546f4-9544-11ed-922d-6045bdc792d8','InstallSDP','执行用户自定义脚本','initScript',1,0,'system',NULL),
    ('e3096844-954e-11ed-922d-6045bdc792dc','8fe546f4-9544-11ed-922d-6045bdc792d8','InstallSDP','查询用户自定义脚本进展','queryPlayJobStatus',2,0,'system',NULL),
    ('e3096902-954e-11ed-922d-6045bdc792dc','8fe546f4-9544-11ed-922d-6045bdc792d8','InstallSDP','执行安装ambari-agent','installAgent',3,0,'system',NULL),
    ('e3096945-954e-11ed-922d-6045bdc792dc','8fe546f4-9544-11ed-922d-6045bdc792d8','InstallSDP','查询安装ambari-agent进度','queryPlayJobStatus',4,0,'system',NULL),
    ('e3096984-954e-11ed-922d-6045bdc792dc','8fe546f4-9544-11ed-922d-6045bdc792d8','clusterservice','ambari-server添加实例','ambariAddHosts',5,0,'system',NULL),
    ('e3096985-954e-11ed-922d-6045bdc792dc','8fe546f4-9544-11ed-922d-6045bdc792d8','clusterservice','将实例添加到ambari配置组中','ambariAddHostsToConfigGroup',6,0,'system',NULL),
    ('e30969cb-954e-11ed-922d-6045bdc792dc','8fe546f4-9544-11ed-922d-6045bdc792d8','clusterservice','ambari-server新增实例配置需要安装的服务','configAddSDP',7,0,'system',NULL),
    ('e30969cc-954e-11ed-922d-6045bdc792dc','8fe546f4-9544-11ed-922d-6045bdc792d8','clusterservice','ambari-server新增实例安装服务','installAddSDP',8,0,'system',NULL);
REPLACE INTO base_cluster_operation_template_activity (activity_id,template_id,activity_type,activity_cnname,activity_name,sort_no,timeout,createdby,created_time) VALUES
    ('e3096a0b-954e-11ed-922d-6045bdc792dc','8fe546f4-9544-11ed-922d-6045bdc792d8','clusterservice','查询ambari-server新增实例安装服务进展','querySDPClusterInstallProcess',9,0,'system',NULL),
    ('e3096a46-954e-11ed-922d-6045bdc792dc','8fe546f4-9544-11ed-922d-6045bdc792d8','InstallSDP','执行集群启动前脚本','beforeClusterStartScript',10,0,'system',NULL),
    ('e3096a85-954e-11ed-922d-6045bdc792dc','8fe546f4-9544-11ed-922d-6045bdc792d8','InstallSDP','查询启动前脚本进展','queryPlayJobStatus',11,0,'system',NULL),
    ('e3096b85-954e-11ed-922d-6045bdc792dc','8fe546f4-9544-11ed-922d-6045bdc792d8','InstallSDP','执行集群启动后脚本','afterClusterCompletedScript',12,0,'system',NULL),
    ('e3096bc3-954e-11ed-922d-6045bdc792dc','8fe546f4-9544-11ed-922d-6045bdc792d8','InstallSDP','查询集群启动后脚本执行进展','queryPlayJobStatus',13,0,'system',NULL),
    ('e3096ac0-954e-11ed-922d-6045bdc792dc','8fe546f4-9544-11ed-922d-6045bdc792d8','clusterservice','新增实例启动服务','startAddHostComponents',14,0,'system',NULL),
    ('e3096afc-954e-11ed-922d-6045bdc792dc','8fe546f4-9544-11ed-922d-6045bdc792d8','clusterservice','查询新增实例启动服务进展','querySDPClusterInstallProcess',15,0,'system',NULL),
    ('e3096b3c-954e-11ed-922d-6045bdc792dc','8fe546f4-9544-11ed-922d-6045bdc792d8','clusterservice','做数据平衡（仅Core节点需要）','dataBalanceForCore',16,0,'system',NULL);

-- 清理异常VM
REPLACE INTO base_cluster_operation_template_activity (activity_id,template_id,activity_type,activity_cnname,activity_name,sort_no,timeout,createdby,created_time) VALUES
    ('25be9c7a-bf77-4b09-8618-b63e51beb94b','8af19974-63ab-41a9-9512-1fb3fc22a2b8','azureVMService','销毁主机','deleteVmsForClearVM',-1,0,'system','2023-02-27 12:03:29'),
    ('559ee730-5d28-45db-a1fc-7026309fee5b','8af19974-63ab-41a9-9512-1fb3fc22a2b8','azureVMService','查询销毁进展','queryClearVmsDeleteJob',0,0,'system','2023-02-27 12:03:29'),
    ('36819b0b-a737-4519-be0f-03985682f19b','8af19974-63ab-41a9-9512-1fb3fc22a2b8','clusterservice','清理集群Host','clearAmbariHostsForClearVM',1,0,'system','2023-03-31 00:03:29');

-- 执行用户脚本
REPLACE INTO base_cluster_operation_template_activity (activity_id,template_id,activity_type,activity_cnname,activity_name,sort_no,timeout,createdby,created_time) VALUES
    ('dc0506c0-838b-11ed-8607-6045bdc792db','80f52443-838b-11ed-8607-6045bdc792d8','InstallSDP','提交用户脚本','runUserCusterScript',0,0,'system',NULL),
    ('dc050236-838b-11ed-8607-6045bdc792db','80f52443-838b-11ed-8607-6045bdc792d8','InstallSDP','执行用户脚本','queryPlayJobStatus',1,0,'system',NULL);

-- 收集日志
REPLACE INTO base_cluster_operation_template_activity (activity_id,template_id,activity_type,activity_cnname,activity_name,sort_no,timeout,createdby,created_time) VALUES
    ('b1c1df71-ad05-11ed-922d-6045bdc792db','84b3739a-a9d7-11ed-922d-6045bdc792d8','clusterservice','启动日志收集任务','collectLogs',-1,0,'system',NULL),
    ('b1c1e6b2-ad05-11ed-922d-6045bdc792db','84b3739a-a9d7-11ed-922d-6045bdc792d8','InstallSDP','查询日志收集任务进展','queryPlayJobStatus',0,0,'system',NULL);

-- 磁盘扩容 89e0a922-adec-11ed-bcf2-6045bdc792d8
REPLACE INTO base_cluster_operation_template_activity (activity_id,template_id,activity_type,activity_cnname,activity_name,sort_no,timeout,createdby,created_time) VALUES
    ('f3d3a64b-7c48-4892-b0f3-2bf177c2e6db','89e0a922-adec-11ed-bcf2-6045bdc792d8','azureVMService','申请磁盘资源扩容','ambariAddPart',-1,0,'system','2023-02-18 00:00:00'),
    ('9e137fc3-0c15-4be1-9712-c23144c44b1b','89e0a922-adec-11ed-bcf2-6045bdc792d8','azureVMService','查询磁盘资源扩容进度','queryAddPartStatus',0,0,'system','2023-02-18 00:00:00'),
    ('e4d3a64b-7c48-4892-b0f3-2bf177c2e6db','89e0a922-adec-11ed-bcf2-6045bdc792d8','InstallSDP','执行磁盘扩容脚本','scaleOutDisk',1,0,'system','2023-02-18 00:00:00'),
    ('e5d3a64b-7c48-4892-b0f3-2bf177c2e6db','89e0a922-adec-11ed-bcf2-6045bdc792d8','InstallSDP','查询磁盘扩容脚本执行进度','queryScaleOutDiskProcess',2,0,'system','2023-02-18 00:00:00');


-- base_cluster_script 增加SDP-2.0脚本
REPLACE INTO base_cluster_script (conf_script_id,release_version,script_name,run_timing,playbook_uri,script_file_uri,extra_vars,sort_no,createdby,created_time) VALUES
    ('d9a6564b-7853-11ed-85b7-6045bdc7fecb','SDP-2.0','安装AmbariServer','install_ambari_server','https://{wgetpath}/sunbox3/shell/ambari.yaml','https://{wgetpath}/sunbox3/shell/ambari-agent.sh,https://{wgetpath}/sunbox3/shell/ambari-server.sh,https://{wgetpath}/sunbox3/shell/initialize.sh,https://{wgetpath}/sunbox3/shell/mountnew.sh','-e "type=server host={ambarihost} dburl={ambaridb} dbport={ambaridbport} dbname={ambaridbname} dbuser={dbuser} dbpass={dbpassword} domain={domain} wgetpath={wgetpath} clustername={clustername} logblob={logblob} username={username}"
    ',0,'system',now()),
    ('f9a6564b-7853-11ed-85b7-6045bdc7fecb','SDP-2.0','安装AmbariAgent','install_ambari_agent','https://{wgetpath}/sunbox3/shell/ambari.yaml','https://{wgetpath}/sunbox3/shell/ambari-agent.sh,https://{wgetpath}/sunbox3/shell/ambari-server.sh,https://{wgetpath}/sunbox3/shell/initialize.sh,https://{wgetpath}/sunbox3/shell/mountnew.sh','-e "type=agent host={ambarihost} dburl={ambaridb} dbport={ambaridbport} dbname={ambaridbname} dbuser={dbuser} dbpass={dbpassword} domain={domain} wgetpath={wgetpath} clustername={clustername} logblob={logblob} username={username}"
',0,'system',now());


-- base_release_apps SDP-2.0 新版本大数据组件
REPLACE INTO base_release_apps (release_version,app_name,app_verison,required,sort_no,created_by,created_time) VALUES
    ('SDP-2.0','HBase','2.4.15',0,8,'sysadmin',now()),
    ('SDP-2.0','HDFS','3.3.3',1,2,'sysadmin',now()),
    ('SDP-2.0','Hive','3.1.3',0,3,'sysadmin',now()),
    ('SDP-2.0','MAPREDUCE2','3.3.3',1,2,'sysadmin',now()),
    ('SDP-2.0','SPARK3','3.3.1',0,5,'sysadmin',now()),
    ('SDP-2.0','SQOOP','1.4.7',0,7,'sysadmin',now()),
    ('SDP-2.0','TEZ','0.10.2',0,4,'sysadmin',now()),
    ('SDP-2.0','YARN','3.3.3',1,2,'sysadmin',now()),
    ('SDP-2.0','ZOOKEEPER','3.5.10',1,1,'sysadmin',now());

-- base_release_apps_config SDP-2.0的配置文件
REPLACE into base_release_apps_config (release_version, app_name, app_config_classification, app_config_file, sort_no, is_delete, createdby, created_time )
select 'SDP-2.0', app_name, app_config_classification, app_config_file, sort_no, is_delete, 'sysadmin', now()
from base_release_apps_config where release_version = 'SDP-1.0'
     and not exists (select 1 from base_release_apps_config where release_version='SDP-2.0');

-- base_release_vm_img SDP-2.0镜像
REPLACE into base_release_vm_img( release_version, vm_role, os_imageid, os_image_type, os_version, createdby, created_time, img_id )
select 'SDP-2.0', vm_role, os_imageid, os_image_type, os_version, 'sysadmin', now(), img_id
from base_release_vm_img
where release_version = 'SDP-1.0'
  and not exists (select 1 from base_release_vm_img where release_version='SDP-2.0');

-- base_scene SDP-2.0 场景
REPLACE INTO base_scene (scene_id,cluster_release_ver,scene_name,scene_desc,created_time,createdby) VALUES
    ('692fc42a-5251-1eb8-f1de-6dbbd3e47530','SDP-2.0','HBASE','HBASE场景', now(),'system'),
    ('6dee2ab7-6e64-80d2-ca67-2a433db39d30','SDP-2.0','DEFAULT','默认场景', now(),'system');
-- base_scene_apps SDP-2.0 场景下的组件
REPLACE INTO base_scene_apps (scene_id,app_name,app_version,required,sort_no) VALUES
    ('692fc42a-5251-1eb8-f1de-6dbbd3e47530','ZOOKEEPER','3.5.10',1,1),
    ('692fc42a-5251-1eb8-f1de-6dbbd3e47530','HDFS','3.3.3',1,2),
    ('692fc42a-5251-1eb8-f1de-6dbbd3e47530','MAPREDUCE2','3.3.3',1,3),
    ('692fc42a-5251-1eb8-f1de-6dbbd3e47530','YARN','3.3.3',1,4),
    ('692fc42a-5251-1eb8-f1de-6dbbd3e47530','HBASE','2.4.15',1,5),
    ('6dee2ab7-6e64-80d2-ca67-2a433db39d30','ZOOKEEPER','3.5.10',1,1),
    ('6dee2ab7-6e64-80d2-ca67-2a433db39d30','HDFS','3.3.3',1,2),
    ('6dee2ab7-6e64-80d2-ca67-2a433db39d30','MAPREDUCE2','3.3.3',1,3),
    ('6dee2ab7-6e64-80d2-ca67-2a433db39d30','YARN','3.3.3',1,4),
    ('6dee2ab7-6e64-80d2-ca67-2a433db39d30','Hive','3.1.3',0,5);
REPLACE INTO base_scene_apps (scene_id,app_name,app_version,required,sort_no) VALUES
    ('6dee2ab7-6e64-80d2-ca67-2a433db39d30','SPARK3','3.3.1',0,6),
    ('6dee2ab7-6e64-80d2-ca67-2a433db39d30','SQOOP','1.4.7',0,7),
    ('6dee2ab7-6e64-80d2-ca67-2a433db39d30','TEZ','0.10.2',0,8);

-- ambari_component_layout SDP-2.0 布局
insert into ambari_component_layout(stack_code, service_code , host_group , component_code , is_ha , state , created_by, created_time, updated_by, updated_time )
select 'SDP-2.0', service_code , host_group , component_code , is_ha , state , 'system', now(), 'system', now()
from ambari_component_layout
where stack_code = 'SDP-1.0'
  and not exists (select 1 from ambari_component_layout where stack_code='SDP-2.0');

-- ambari_config_item SDP-2.0 配置项
insert into ambari_config_item (stack_code, service_code, component_code , config_type_code , `key`, value,
                                is_content_prop , is_dynamic , dynamic_type , item_type , state , created_by , created_time ,
                                updated_by , updated_time)
select 'SDP-2.0' as stack_code, service_code, component_code , config_type_code , `key`, value,
       is_content_prop , is_dynamic , dynamic_type , item_type , state , 'system' as created_by , now() as created_time ,
       'system' as updated_by , now() as updated_time
from ambari_config_item aci
where stack_code = 'SDP-1.0'
  and not exists (select 1 from ambari_config_item where stack_code='SDP-2.0');



