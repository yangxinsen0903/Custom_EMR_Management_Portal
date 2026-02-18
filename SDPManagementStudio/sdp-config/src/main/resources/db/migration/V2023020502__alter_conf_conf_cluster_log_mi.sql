drop procedure if exists schema_change;
DELIMITER //
CREATE PROCEDURE schema_change()
begin
    declare var_db_name varchar(100);
    declare var_table_name varchar(100);
    declare var_table_column_client_id varchar(100);
    declare var_table_column_tenant_id varchar(100);
    declare var_table_column varchar(100);
    declare column_type varchar(100);
    declare comment_value varchar(100);
    declare table_count int;
    declare start_index int;
    set var_db_name = (select database());
    set var_table_name = 'conf_cluster';
    set var_table_column_client_id = 'log_mi_client_id';
    set var_table_column_tenant_id = 'log_mi_tenant_id';
    set column_type = 'varchar(64)';
    set comment_value = '日志桶节点托管MI的TenantId';

    set @table_name = var_table_name;
    IF EXISTS (SELECT * FROM information_schema.columns WHERE table_schema = var_db_name and table_name = @table_name ) THEN
        IF NOT EXISTS (SELECT * FROM information_schema.columns WHERE table_schema = var_db_name and table_name = @table_name AND column_name = var_table_column_tenant_id ) THEN
            set @statement = CONCAT('alter table ',var_db_name, '.',@table_name, ' add column ',var_table_column_tenant_id,' ',column_type,' NULL COMMENT "',comment_value,'"');
            prepare STMT from @statement;
            execute STMT;
        END IF;
    END IF;
    

    set comment_value = '日志桶节点托管MI的ClientId';
    IF EXISTS (SELECT * FROM information_schema.columns WHERE table_schema = var_db_name and table_name = @table_name ) THEN
        IF NOT EXISTS (SELECT * FROM information_schema.columns WHERE table_schema = var_db_name and table_name = @table_name AND column_name = var_table_column_client_id ) THEN
            set @statement = CONCAT('alter table ',var_db_name, '.',@table_name, ' add column ',var_table_column_client_id,' ',column_type,' NULL COMMENT "',comment_value,'"');
            prepare STMT from @statement;
            execute STMT;
        END IF;
    END IF;
END //
DELIMITER ;
call schema_change();
drop procedure if exists schema_change;