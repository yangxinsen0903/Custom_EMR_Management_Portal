REPLACE INTO base_cluster_operation_template (template_id, release_version, operation_name,
                                              operation_description, is_delete, createdby, created_time)
VALUES ('fe02abfb-371e-47b8-8329-70489b8d284b', 'SDP-1.0', 'pv2DiskAdjust', 'pv2磁盘性能调整', 0, 'system',
        '2024-09-02 12:28:22');

REPLACE INTO base_cluster_operation_template_activity (activity_id, template_id, activity_type, activity_cnname,
                                                       activity_name, sort_no, timeout, createdby, created_time)
VALUES ('56d2aad2-3960-46a0-80fe-33ee7d4a4d61', 'fe02abfb-371e-47b8-8329-70489b8d284b', 'azureVMService',
        '请求pv2磁盘性能调整', 'diskPerformanceAdjust', 0, 0, 'system', '2024-09-02 12:28:22');

REPLACE INTO base_cluster_operation_template_activity (activity_id, template_id, activity_type, activity_cnname,
                                                       activity_name, sort_no, timeout, createdby, created_time)
VALUES ('567c1fa1-9ee8-4f9a-b97f-25b0ee5d28b5', 'fe02abfb-371e-47b8-8329-70489b8d284b', 'azureVMService',
        '查询pv2磁盘性能调整结果', 'queryDiskPerformanceAdjust', 1, 0, 'system', '2024-09-02 12:28:22');
