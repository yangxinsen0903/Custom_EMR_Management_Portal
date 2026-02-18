REPLACE INTO base_cluster_operation_template (template_id, release_version, operation_name,
                                             operation_description, is_delete, createdby, created_time)
VALUES ('15cab9a5-ccf9-4ae1-96e9-b48fd1969843', 'SDP-2.0', 'pv2DiskAdjust', 'pv2磁盘性能调整', 0, 'system',
        '2024-09-02 12:28:22');

REPLACE INTO base_cluster_operation_template_activity (activity_id, template_id, activity_type, activity_cnname,
                                                      activity_name, sort_no, timeout, createdby, created_time)
VALUES ('e9defdee-9127-4f7a-a74b-6376339ba3a2', '15cab9a5-ccf9-4ae1-96e9-b48fd1969843', 'azureVMService',
        '请求pv2磁盘性能调整', 'diskPerformanceAdjust', 0, 0, 'system', '2024-09-02 12:28:22');

REPLACE INTO base_cluster_operation_template_activity (activity_id, template_id, activity_type, activity_cnname,
                                                      activity_name, sort_no, timeout, createdby, created_time)
VALUES ('3a318c1c-0069-4f85-99a2-bfecce6c6ace', '15cab9a5-ccf9-4ae1-96e9-b48fd1969843', 'azureVMService',
        '查询pv2磁盘性能调整结果', 'queryDiskPerformanceAdjust', 1, 0, 'system', '2024-09-02 12:28:22');