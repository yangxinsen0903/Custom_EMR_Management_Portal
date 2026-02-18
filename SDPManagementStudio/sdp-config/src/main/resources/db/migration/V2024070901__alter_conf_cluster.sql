drop procedure if exists schema_change;
DELIMITER //
create procedure schema_change()
begin
    declare var_db_name varchar(100);
    declare var_table_name varchar(100);
    declare var_table_column varchar(100);
    declare column_type varchar(100);
    declare comment_value varchar(100);
    declare table_count int;
    declare start_index int;
    set var_db_name = (select database());
    set var_table_name = 'conf_cluster';
    set var_table_column = 'is_white_addr';
    set column_type = 'int';
    set comment_value = '是否加入直接销毁白名单,是空或1,否0';
    set table_count = 1 ;
    while table_count > 0 do
            set @table_name = var_table_name;
            if table_count <> 1 then
                set @table_name = CONCAT(@table_name, '_', table_count - 1);
            end if;
            IF EXISTS (SELECT * FROM information_schema.columns WHERE table_schema = var_db_name and table_name = @table_name ) THEN
                IF NOT EXISTS (SELECT * FROM information_schema.columns WHERE table_schema = var_db_name and table_name = @table_name AND column_name = var_table_column ) THEN
                    set @statement = CONCAT('alter table ',var_db_name, '.',@table_name, ' add column ',var_table_column,' ',column_type,' COMMENT "',comment_value,'"');
                    prepare STMT from @statement;
                    execute STMT;
                END IF;
            END IF;
            set table_count = table_count - 1;
        end while;
end //
DELIMITER ;
call schema_change();
drop procedure if exists schema_change;
 
create table if not exists cluster_destroy_task
(
    id bigint AUTO_INCREMENT  NOT NULL COMMENT '自增主键' primary key,
    cluster_id        varchar(40)  not null comment '集群ID' ,
    cluster_name      varchar(200) null comment '集群名称',
    is_white_addr     int          null comment '是否加入直接销毁白名单,是空或1,否0',
    start_destroy_time  datetime      null comment '开始销毁时间',
    end_destroy_time   datetime      null comment '结束销毁时间',
    destroy_task_id   varchar(40)  null comment '销毁任务ID',
    destroy_status    varchar(20)  null comment '销毁状态:待销毁1，销毁中2，已销毁3，销毁失败0',
    f_del     int        DEFAULT  0 comment '是否强制删除,是1,否0',
    created_time DATETIME DEFAULT NULL COMMENT '创建时间',
    createdby varchar(60) DEFAULT NULL  COMMENT '创建人' ,
    modified_time DATETIME DEFAULT NULL COMMENT '修改时间',
    modifiedby varchar(60) DEFAULT NULL  COMMENT '修改人'
) ENGINE=InnoDB comment '集群销毁任务表' DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ;

call create_index( 'cluster_destroy_task', 'idx_cluster_id', ' (cluster_id)' );
