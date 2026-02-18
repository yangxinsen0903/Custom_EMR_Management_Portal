CREATE TABLE if not exists `info_group_full_custody_elastic_scaling_log` (
  `es_full_log_id` bigint NOT NULL AUTO_INCREMENT,
  `es_rule_id` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `cluster_id` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `group_name` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `is_start_scaling` int DEFAULT NULL,
  `scaling_type` varchar(8) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `created_time` timestamp NULL DEFAULT NULL,
  `compute_value` double(20,2) DEFAULT NULL,
  `task_result` varchar(8) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `task_result_message` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `task_id` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `metric_values` text COLLATE utf8mb4_general_ci,
  `scaling_count` int DEFAULT NULL,
  PRIMARY KEY (`es_full_log_id`),
  KEY `clusterId_groupName` (`cluster_id`,`group_name`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=15207 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;





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
    set var_table_name = 'conf_group_elastic_scaling';
    set var_table_column = 'is_graceful_scalein';
    set column_type = 'int';
    set comment_value = '是否优雅缩容 1：是，0：不是';
    set table_count = 1 ;
    while table_count > 0 do
            set @table_name = var_table_name;
            if table_count <> 1 then
                set @table_name = CONCAT(@table_name, '_', table_count - 1);
            end if;
            IF EXISTS (SELECT * FROM information_schema.columns WHERE table_schema = var_db_name and table_name = @table_name ) THEN
                IF NOT EXISTS (SELECT * FROM information_schema.columns WHERE table_schema = var_db_name and table_name = @table_name AND column_name = var_table_column ) THEN
                    set @statement = CONCAT('alter table ',var_db_name, '.',@table_name, ' add column ',var_table_column,' ',column_type,' DEFAULT NULL COMMENT "',comment_value,'"');
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
    set var_table_name = 'conf_group_elastic_scaling';
    set var_table_column = 'is_full_custody';
    set column_type = 'int';
    set comment_value = '是否开启全托管 1：开启，0：不开启';
    set table_count = 1 ;
    while table_count > 0 do
            set @table_name = var_table_name;
            if table_count <> 1 then
                set @table_name = CONCAT(@table_name, '_', table_count - 1);
            end if;
            IF EXISTS (SELECT * FROM information_schema.columns WHERE table_schema = var_db_name and table_name = @table_name ) THEN
                IF NOT EXISTS (SELECT * FROM information_schema.columns WHERE table_schema = var_db_name and table_name = @table_name AND column_name = var_table_column ) THEN
                    set @statement = CONCAT('alter table ',var_db_name, '.',@table_name, ' add column ',var_table_column,' ',column_type,' DEFAULT NULL COMMENT "',comment_value,'"');
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
    set var_table_name = 'conf_group_elastic_scaling';
    set var_table_column = 'scalein_waiting_time';
    set column_type = 'int';
    set comment_value = '优雅缩容等待时间单位：分钟';
    set table_count = 1 ;
    while table_count > 0 do
            set @table_name = var_table_name;
            if table_count <> 1 then
                set @table_name = CONCAT(@table_name, '_', table_count - 1);
            end if;
            IF EXISTS (SELECT * FROM information_schema.columns WHERE table_schema = var_db_name and table_name = @table_name ) THEN
                IF NOT EXISTS (SELECT * FROM information_schema.columns WHERE table_schema = var_db_name and table_name = @table_name AND column_name = var_table_column ) THEN
                    set @statement = CONCAT('alter table ',var_db_name, '.',@table_name, ' add column ',var_table_column,' ',column_type,' DEFAULT NULL COMMENT "',comment_value,'"');
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
    set var_table_name = 'conf_group_elastic_scaling';
    set var_table_column = 'enable_beforestart_script';
    set column_type = 'int';
    set comment_value = '是否执行启动前脚本 1：是，0：不是';
    set table_count = 1 ;
    while table_count > 0 do
            set @table_name = var_table_name;
            if table_count <> 1 then
                set @table_name = CONCAT(@table_name, '_', table_count - 1);
            end if;
            IF EXISTS (SELECT * FROM information_schema.columns WHERE table_schema = var_db_name and table_name = @table_name ) THEN
                IF NOT EXISTS (SELECT * FROM information_schema.columns WHERE table_schema = var_db_name and table_name = @table_name AND column_name = var_table_column ) THEN
                    set @statement = CONCAT('alter table ',var_db_name, '.',@table_name, ' add column ',var_table_column,' ',column_type,' DEFAULT NULL COMMENT "',comment_value,'"');
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
    set var_table_name = 'conf_group_elastic_scaling';
    set var_table_column = 'enable_afterstart_script';
    set column_type = 'int';
    set comment_value = '是否执行启动后脚本 1：是，0：不是';
    set table_count = 1 ;
    while table_count > 0 do
            set @table_name = var_table_name;
            if table_count <> 1 then
                set @table_name = CONCAT(@table_name, '_', table_count - 1);
            end if;
            IF EXISTS (SELECT * FROM information_schema.columns WHERE table_schema = var_db_name and table_name = @table_name ) THEN
                IF NOT EXISTS (SELECT * FROM information_schema.columns WHERE table_schema = var_db_name and table_name = @table_name AND column_name = var_table_column ) THEN
                    set @statement = CONCAT('alter table ',var_db_name, '.',@table_name, ' add column ',var_table_column,' ',column_type,' DEFAULT NULL COMMENT "',comment_value,'"');
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