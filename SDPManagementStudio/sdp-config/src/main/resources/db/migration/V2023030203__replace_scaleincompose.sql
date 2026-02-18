
REPLACE INTO base_cluster_operation_template_activity (activity_id,template_id,activity_type,activity_cnname,activity_name,sort_no,timeout,createdby,created_time) VALUES
('370951c7-1156-41c8-8bf6-b66e4298983b','34438518-9544-11ed-922d-6045bdc792d8','InstallSDP','调整HDFS-BalanceBandWidth','scaleUpHdfsBalanceBandWidth',-1,0,'system',NULL),
('cb40ccdc-255c-4b3b-8d4e-90e9f0c38fe8','34438518-9544-11ed-922d-6045bdc792d8','InstallSDP','查询调整HDFS-BalanceBandWidth结果','queryPlayJobStatus',0,0,'system',NULL),
('cd35e91f-9c00-11ed-922d-6045bdc792d8','34438518-9544-11ed-922d-6045bdc792d8','clusterservice','执行DataNode节点DECOMMISSION','dataNodeDecommionsion',1,0,'system',NULL),
('cd35eef5-9c00-11ed-922d-6045bdc792d8','34438518-9544-11ed-922d-6045bdc792d8','clusterservice','查询DataNode节点DECOMMISSION执行状态','queryDataNodeDecommission',2,0,'system',NULL),
('76fc44c0-b18a-4f73-929e-c1ad8c9c81fe','34438518-9544-11ed-922d-6045bdc792d8','InstallSDP','调整HDFS-BalanceBandWidth','scaleDownHdfsBalanceBandWidth',3,0,'system',NULL),
('10f6c5ba-de57-4293-9e72-b2ea30aab996','34438518-9544-11ed-922d-6045bdc792d8','InstallSDP','查询调整HDFS-BalanceBandWidth结果','queryPlayJobStatus',4,0,'system',NULL),
('cd35f066-9c00-11ed-922d-6045bdc792d8','34438518-9544-11ed-922d-6045bdc792d8','clusterservice','执行NodeManger节点DECOMMISSION','nodeManagerDecommionsion',5,0,'system',NULL),
('cd35f152-9c00-11ed-922d-6045bdc792d8','34438518-9544-11ed-922d-6045bdc792d8','clusterservice','查询NodeManger节点DECOMMISSION执行状态','queryNodeManagerDecommission',6,0,'system',NULL),
('cd35f067-9c00-11ed-922d-6045bdc792d8','34438518-9544-11ed-922d-6045bdc792d8','clusterservice','执行HbaseRegionServer节点DECOMMISSION','hbaseRegionServerDecommionsion',7,0,'system',NULL),
('cd35f158-9c00-11ed-922d-6045bdc792d8','34438518-9544-11ed-922d-6045bdc792d8','clusterservice','查询HbaseRegionServer节点DECOMMISSION执行状态','queryHbaseRegionServerDecommission',8,0,'system',NULL),
('cd35f159-9c00-11ed-922d-6045bdc792d8','34438518-9544-11ed-922d-6045bdc792d8','clusterservice','优雅缩容等待','gracefullWating',9,0,'system',NULL),
('cd35f233-9c00-11ed-922d-6045bdc792d8','34438518-9544-11ed-922d-6045bdc792d8','clusterservice','执行关闭组件命令','closeComponentByHost',10,0,'system',NULL),
('cd35f45b-9c00-11ed-922d-6045bdc792d8','34438518-9544-11ed-922d-6045bdc792d8','clusterservice','查询关闭组件命令状态','queryCloseComponentStatus',11,0,'system',NULL),
('cd35f53f-9c00-11ed-922d-6045bdc792d8','34438518-9544-11ed-922d-6045bdc792d8','clusterservice','ambari-server删除主机','deleteAmbariHosts',12,0,'system',NULL),
('cd35f54f-9c00-11ed-922d-6045bdc792d8','34438518-9544-11ed-922d-6045bdc792d8','clusterservice','ambari-server删除配置组','deleteAmbariHostGroup',13,0,'system','2023-02-27 12:03:29'),
('cd35f62b-9c00-11ed-922d-6045bdc792d8','34438518-9544-11ed-922d-6045bdc792d8','clusterservice','检查数据块 hdfs fsck （仅Core节点需要）','checkDataForCore',14,0,'system','2023-02-27 12:03:29'),
('cd35f6fe-9c00-11ed-922d-6045bdc792d8','34438518-9544-11ed-922d-6045bdc792d8','azureVMService','销毁主机','deleteVmsForScaleIn',15,0,'system','2023-02-27 12:03:29'),
('cd35f768-9c00-11ed-922d-6045bdc792d8','34438518-9544-11ed-922d-6045bdc792d8','azureVMService','查询销毁进展','queryScaleInVmsDeleteJob',16,0,'system','2023-02-27 12:03:29'),
('fd35f62b-9c00-11ed-922d-6045bdc792d8','34438518-9544-11ed-922d-6045bdc792d8','clusterservice','清理AmbariHost','clearAmbariHosts',17,0,'system','2023-02-22 15:39:27'),
('89e0318c-5a2e-49b5-b0e6-9fdcae64a7a9','34438518-9544-11ed-922d-6045bdc792d8','clusterservice','清理Ganglia数据','clearGangliaData',18,0,'system',NULL),
('c889e9cf-bb20-4ca3-ad51-a234a10a52bf','34438518-9544-11ed-922d-6045bdc792d8','InstallSDP','查询清理Ganglia数据进度','queryPlayJobStatus',19,0,'system',NULL);

