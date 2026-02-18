create table if not exists info_cluster_info_collect_log(
    id VARCHAR(64) PRIMARY KEY ,
    cluster_id VARCHAR(64),
    cluster_name VARCHAR(64),
    task_id VARCHAR(64),
    state VARCHAR(16),
    create_time TIMESTAMP,
    finish_time TIMESTAMP,
    file_path VARCHAR(255),
    host_ips TEXT,
    ansible_Transaction_Id VARCHAR(64),
    INDEX `idx_cluster_id_state`(`cluster_id`, `state`) USING BTREE
 );



