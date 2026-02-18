
REPLACE INTO base_cluster_operation_template_activity (activity_id,template_id,activity_type,activity_cnname,activity_name,sort_no,timeout,createdby,created_time)
VALUES
       ('f3d3a64b-7c48-4892-b0f3-2bf177c2e6d5','f9e0a922-adec-11ed-bcf2-6045bdc792d8','azureVMService','申请磁盘资源扩容','ambariAddPart',-1,0,'system','2023-02-18 00:00:00'),
       ('9e137fc3-0c15-4be1-9712-b23144c44b1b','f9e0a922-adec-11ed-bcf2-6045bdc792d8','azureVMService','查询磁盘资源扩容进度','queryAddPartStatus',0,0,'system','2023-02-18 00:00:00'),
       ('e4d3a64b-7c48-4892-b0f3-2bf177c2e6d5','f9e0a922-adec-11ed-bcf2-6045bdc792d8','InstallSDP','执行磁盘扩容脚本','scaleOutDisk',1,0,'system','2023-02-18 00:00:00'),
       ('e5d3a64b-7c48-4892-b0f3-2bf177c2e6d5','f9e0a922-adec-11ed-bcf2-6045bdc792d8','InstallSDP','查询磁盘扩容脚本执行进度','queryScaleOutDiskProcess',2,0,'system','2023-02-18 00:00:00');
