
REPLACE INTO  base_cluster_operation_template (template_id,release_version,operation_name,operation_description,is_delete,createdby,created_time) VALUES
    ('6af19974-63ab-41a9-9512-1fb3fc22a2b4','SDP-1.0','clearvms','清理异常VM',0,'',NULL);

REPLACE INTO base_cluster_operation_template_activity (activity_id,template_id,activity_type,activity_cnname,activity_name,sort_no,timeout,createdby,created_time) VALUES
('25be9c7a-bf77-4b09-8618-b63e51beb941','6af19974-63ab-41a9-9512-1fb3fc22a2b4','azureVMService','销毁主机','deleteVmsForClearVM',-1,0,'system','2023-02-27 12:03:29'),
('559ee730-5d28-45db-a1fc-7026309fee56','6af19974-63ab-41a9-9512-1fb3fc22a2b4','azureVMService','查询销毁进展','queryClearVmsDeleteJob',0,0,'system','2023-02-27 12:03:29');
