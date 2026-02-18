
-- info_cluster_operation_plan_activity_log 增加索引
call create_index( 'info_cluster_operation_plan_activity_log', 'idx_plan_id', '(plan_id)' );
