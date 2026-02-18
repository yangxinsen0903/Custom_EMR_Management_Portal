

Replace INTO base_cluster_operation_template
    (template_id,release_version,operation_name,operation_description,is_delete,createdby,created_time) VALUES
    ('e4b3739a-a9d7-11ed-922d-6045bdc792d8','SDP-1.0','collectLogs','收集日志',0,'',NULL);

Replace INTO base_cluster_operation_template_activity
(activity_id,template_id,activity_type,activity_cnname,activity_name,sort_no,timeout,createdby,created_time)
VALUES
('b1c1df71-ad05-11ed-922d-6045bdc792d8','e4b3739a-a9d7-11ed-922d-6045bdc792d8','clusterservice','启动日志收集任务','collectLogs',-1,0,'system',NULL),
('b1c1e6b2-ad05-11ed-922d-6045bdc792d8','e4b3739a-a9d7-11ed-922d-6045bdc792d8','InstallSDP','查询日志收集任务进展','queryPlayJobStatus',0,0,'system',NULL);

