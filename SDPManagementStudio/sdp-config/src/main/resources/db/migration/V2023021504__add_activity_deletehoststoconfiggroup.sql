replace INTO base_cluster_operation_template_activity (activity_id,template_id,activity_type,activity_cnname,activity_name,sort_no,timeout,createdby,created_time) VALUES
	 ('cd35f54f-9c00-11ed-922d-6045bdc792d8','34438518-9544-11ed-922d-6045bdc792d8','clusterservice','ambari-server删除配置组','deleteAmbariHostGroup',9,0,'system',now()),
	 ('cd35f62b-9c00-11ed-922d-6045bdc792d8','34438518-9544-11ed-922d-6045bdc792d8','clusterservice','检查数据块 hdfs fsck （仅Core节点需要）','checkDataForCore',10,0,'system',now()),
	 ('cd35f6fe-9c00-11ed-922d-6045bdc792d8','34438518-9544-11ed-922d-6045bdc792d8','azureVMService','销毁主机','deleteVmsForScaleIn',11,0,'system',now()),
	 ('cd35f768-9c00-11ed-922d-6045bdc792d8','34438518-9544-11ed-922d-6045bdc792d8','azureVMService','查询销毁进展','queryScaleInVmsDeleteJob',12,0,'system',now());
