
REPLACE INTO base_cluster_operation_template
    (template_id,release_version,operation_name,operation_description,is_delete,createdby,created_time) VALUES
    ('29a539c9-a615-11ed-922d-6045bdc792d8','SDP-1.0','restartservice','重启服务',0,'',NULL);
REPLACE INTO base_cluster_operation_template_activity
    (activity_id,template_id,activity_type,activity_cnname,activity_name,sort_no,timeout,createdby,created_time) VALUES
    ('c70a3244-a616-11ed-922d-6045bdc792d8','29a539c9-a615-11ed-922d-6045bdc792d8','clusterservice','启动重启服务任务','restartSDPService',-1,0,'system',NULL),
    ('c70a3521-a616-11ed-922d-6045bdc792d8','29a539c9-a615-11ed-922d-6045bdc792d8','clusterservice','查询重启服务结果','QuerySDPServiceRestartProcess',1,0,'system',NULL);
