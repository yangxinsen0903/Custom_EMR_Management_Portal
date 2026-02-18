drop procedure if exists schema_change;
DELIMITER //
create procedure schema_change()
begin
    declare var_db_name varchar(100);
    declare var_table_name varchar(100);
    declare var_table_column varchar(100);

    set var_db_name = (select database());
    set var_table_name = 'conf_cluster_op_task';
    set var_table_column = 'group_id';

    IF EXISTS (SELECT * FROM information_schema.columns WHERE table_schema = var_db_name and table_name = var_table_name ) THEN
        IF NOT EXISTS (SELECT * FROM information_schema.columns WHERE table_schema = var_db_name and table_name = var_table_name AND column_name = var_table_column ) THEN
            ALTER TABLE conf_cluster_op_task ADD group_id varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '实例组Id';
        END IF;
    END IF;
end //
DELIMITER ;
call schema_change();
drop procedure if exists schema_change;