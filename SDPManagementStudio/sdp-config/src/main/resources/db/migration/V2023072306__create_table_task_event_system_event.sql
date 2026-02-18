-- 创建任务事件表
create table if not exists task_event (
    task_event_id          varchar(64) not null comment '事件ID，UUID类型',
    cluster_id             varchar(64) comment '集群ID',
    cluster_name           varchar(48) comment '集群名称',
    vm_role                varchar(16) comment '主机所在实例组的角色名称',
    group_name             varchar(48) comment '任务执行的实例组名称',
    event_type             varchar(48) comment '事件类型，字符串形式，见枚举类：TaskEventType',
    plan_id                varchar(48) comment '任务计划ID',
    plan_name              varchar(48) comment '任务计划名称',
    plan_activity_log_id   varchar(48) comment '任务计划执行出问题的活动ID',
    plan_activity_log_name varchar(48) comment '任务任务出问题的活动名称',
    event_trigger_time     datetime null comment '事件触发时间',
    event_desc             text comment '事件的描述',
    primary key (task_event_id)) comment='SDP执行任务事件表，记录所有发生的失败事件';

call create_index( 'task_event', 'idx_cluster_id', '(cluster_id)' );
call create_index( 'task_event', 'idx_event_trigger_time', '(event_trigger_time)' );
call create_index( 'task_event', 'idx_plan_id', '(plan_id)' );


-- 创建系统事件表
create table if not exists system_event (
      system_event_id    varchar(48) not null comment '主键，UUID',
      event_trigger_time datetime null comment '事件触发时间',
      event_type         varchar(48) comment '事件类型，见代码中枚举：SystemEventType',
      event_desc         text comment '事件详细描述',
      primary key (system_event_id)) comment='系统事件表';

call create_index( 'system_event', 'idx_event_trigger_time', '(event_trigger_time)' );
