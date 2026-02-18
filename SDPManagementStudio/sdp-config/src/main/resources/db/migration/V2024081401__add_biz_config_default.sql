

REPLACE INTO biz_config (id, category, name, cfg_key, cfg_value, remark, sort_no, state, create_time, update_time) values
    (1823545689113317376, '工单系统配置','工单系统URL','workflow.url','','工单系统URL',1 ,'VALID', now(),now());

REPLACE INTO biz_config (id, category, name, cfg_key, cfg_value, remark, sort_no, state, create_time, update_time) values
    (1823545689113317377, '工单系统配置','工单系统Token','workflow.token','','工单系统提供的token',2 ,'VALID', now(),now());

REPLACE INTO biz_config (id, category, name, cfg_key, cfg_value, remark, sort_no, state, create_time, update_time) values
    (1823545689113317378, '工单系统配置','workflow_name','workflow.name','','工单系统中提前创建的工作流名称',3 ,'VALID', now(),now());

REPLACE INTO biz_config (id, category, name, cfg_key, cfg_value, remark, sort_no, state, create_time, update_time) values
    (1823545689113317379, '工单系统配置','workflow_identifier','workflow.identifier','','工单系统中提前创建的工作流标识',4 ,'VALID', now(),now());
