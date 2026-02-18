
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
    set var_table_name = 'conf_cluster_vm';
    set var_table_column = 'purchase_priority';
    set column_type = 'int';
    set comment_value = '购买优先级';
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