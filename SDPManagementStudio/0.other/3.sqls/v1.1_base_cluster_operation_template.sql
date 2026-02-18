

replace INTO sdpms.base_cluster_operation_template (template_id,release_version,operation_name,operation_description,is_delete,createdby,created_time) VALUES
    ('90f52443-838b-11ed-8607-6045bdc792d8','SDP-1.0','runuserscript','执行用户脚本',0,'',NULL);

replace INTO sdpms.base_cluster_operation_template_activity (activity_id,template_id,activity_type,activity_cnname,activity_name,sort_no,timeout,createdby,created_time) VALUES
                                                                                                                                                                       ('dc050236-838b-11ed-8607-6045bdc792d8','90f52443-838b-11ed-8607-6045bdc792d8','InstallSDP','执行用户脚本','queryPlayJobStatus',1,0,'system',NULL),
                                                                                                                                                                       ('dc0506c0-838b-11ed-8607-6045bdc792d8','90f52443-838b-11ed-8607-6045bdc792d8','InstallSDP','提交用户脚本','runUserCusterScript',0,0,'system',NULL);
-- 调整单个步骤timeout时间
update sdpms.base_cluster_operation_template_activity  set timeout =3600
where activity_id  in ('5c4dc157-78d0-11ed-85b7-6045bdc7fdca','876f5319-79f0-11ed-85b7-6045bdc7fdca');