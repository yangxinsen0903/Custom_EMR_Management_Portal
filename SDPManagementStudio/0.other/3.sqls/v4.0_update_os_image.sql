REPLACE INTO base_image_scripts (img_script_id, img_id, script_name, run_timing, playbook_uri, script_file_uri,
                                extra_vars, sort_no, createdby, created_time)
VALUES ('12e8b19011864edf9bdfa78t5b35f615', '3868275d0f2754c72e9d01116db73e04', '清理集群ambari host历史数据',
        'clean_ambari_history', '', 'https://{wgetpath}/sunbox3/shell/3.0.3/SDP2_0/clean_ambari_host.sh', '', 0,
        'niyang', '2023-07-16 08:18:32');

REPLACE INTO base_image_scripts (img_script_id, img_id, script_name, run_timing, playbook_uri, script_file_uri,
                                extra_vars, sort_no, createdby, created_time)
VALUES ('1989b19011864edf9bdfa78t5b35fiun', 'c0122bbd86cd49f6881286c46d6e6226', '清理集群ambari host历史数据',
        'clean_ambari_history', '', 'https://{wgetpath}/sunbox3/shell/3.0.3/SDP2_0/clean_ambari_host.sh', '', 0,
        'niyang', '2023-07-16 08:18:32');



REPLACE INTO base_image_scripts (img_script_id, img_id, script_name, run_timing, playbook_uri, script_file_uri,
                                extra_vars, sort_no, createdby, created_time)
VALUES ('17e8b19011864cdf9bdfa7875b35f615', 'c0122bbd86cd49f6881286c46d6e6226', '执行HDFS FSCK', 'run_hdfs_fsck', '',
        'https://{wgetpath}/sunbox3/shell/3.0.3/SDP2_0/hdfs_del.sh', '', 0, 'niyang', '2023-07-16 08:18:32');
REPLACE INTO base_image_scripts (img_script_id, img_id, script_name, run_timing, playbook_uri, script_file_uri,
                                extra_vars, sort_no, createdby, created_time)
VALUES ('c522467addc1e5f1223a8bf12887e626', '3868275d0f2754c72e9d01116db73e04', '执行HDFS FSCK', 'run_hdfs_fsck', '',
        'https://{wgetpath}/sunbox3/shell/3.0.3/SDP2_0/hdfs_del.sh', '', 0, 'niyang', '2024-06-23 09:52:12');