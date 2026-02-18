
/* Create table in target */
CREATE TABLE IF NOT EXISTS sdpms.base_script(
                                            `script_id` varchar(40)   NOT NULL  COMMENT '脚本ID（UUID）' ,
                                            `script_name` varchar(255)   NOT NULL  COMMENT '脚本名' ,
                                            `blob_path` varchar(255)   NULL  COMMENT '脚本在blob上的存储路径' ,
                                            `upload_time` datetime NULL  COMMENT '脚本上传时间' ,
                                            `remark` varchar(1024)   NULL  COMMENT '脚本备注' ,
                                            `state` varchar(32)   NOT NULL  DEFAULT 'VALID' COMMENT '脚本状态;VALID，INVALID，DELETED' ,
                                            PRIMARY KEY (`script_id`)
) ENGINE=InnoDB DEFAULT CHARSET='utf8mb4' COLLATE='utf8mb4_general_ci' COMMENT='用户上传的需要执行的脚本库';


-- 更新playbook 参数
update sdpms.base_cluster_script  set extra_vars='-e "type=server host={ambarihost} dburl={ambaridb} dbport={ambaridbport} dbname={ambaridbname} dbuser={dbuser} dbpass={dbpassword} domain={domain} wgetpath={wgetpath} clustername={clustername} logblob={logblob} username={username}"
' where conf_script_id ='d9a6564b-7853-11ed-85b7-6045bdc7fdca';

update sdpms.base_cluster_script  set extra_vars='-e "type=agent host={ambarihost} dburl={ambaridb} dbport={ambaridbport} dbname={ambaridbname} dbuser={dbuser} dbpass={dbpassword} domain={domain} wgetpath={wgetpath} clustername={clustername} logblob={logblob} username={username}"
' where conf_script_id ='f9a6564b-7853-11ed-85b7-6045bdc7fdca';

update sdpms.base_cluster_script  set script_file_uri='https://{wgetpath}/sunbox3/shell/ambari-agent.sh,https://{wgetpath}/sunbox3/shell/ambari-server.sh,https://{wgetpath}/sunbox3/shell/initialize.sh,https://{wgetpath}/sunbox3/shell/mountnew.sh' where release_version='SDP-1.0';
