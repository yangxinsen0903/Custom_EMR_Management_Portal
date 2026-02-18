replace INTO base_cluster_operation_template (template_id,release_version,operation_name,operation_description,is_delete,createdby,created_time)
values ('f9e0a922-adec-11ed-bcf2-6045bdc792d8','SDP-1.0','scaleoutpart','磁盘扩容',0,'system','2023-02-18');


replace INTO base_cluster_operation_template_activity (activity_id,template_id,activity_type,activity_cnname,activity_name,sort_no,timeout,createdby,created_time)
values ('f3d3a64b-7c48-4892-b0f3-2bf177c2e6d5','f9e0a922-adec-11ed-bcf2-6045bdc792d8','azureVMService','磁盘扩容','ambariAddPart',1,0,'system','2023-02-18'),
       ('9e137fc3-0c15-4be1-9712-b23144c44b1b','f9e0a922-adec-11ed-bcf2-6045bdc792d8','azureVMService','查询扩容进展','queryAddPartStatus',2,0,'system','2023-02-18');
