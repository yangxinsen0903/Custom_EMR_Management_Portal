-- 删除存储过程
drop procedure if exists schema_change;
DELIMITER //
-- 创建存储过程
CREATE PROCEDURE `schema_change`()
begin
	declare var_db_name varchar(100);
    declare var_table_name varchar(100);
    declare var_table_column varchar(100);
    declare column_type varchar(100);
    declare comment_value varchar(100);
    declare table_count int;
    declare start_index int;

    set var_db_name = (select database());
    set var_table_name = 'conf_scaling_task';
    set @beforeColumn = 'enable_beforestart_script';
    set @afterColumn = 'enable_afterstart_script';
    set @ruleTableName = 'conf_group_elastic_scaling_rule';
    set column_type = 'int';

    set @beforeColumnComment = '是否执行集群启动前脚本。1：执行  0：不执行  ';
    set @afterColumnComment = '是否执行集群启动后脚本。1：执行  0：不执行  ';

    set table_count = 1 ;
    while table_count > 0 do
        set @table_name = var_table_name;
        if table_count <> 1 then
            set @table_name = CONCAT(@table_name, '_', table_count - 1);
        end if;

        -- 增加 conf_scaling_task 表的列： enableBeforestartScript
        IF EXISTS (SELECT * FROM information_schema.columns WHERE table_schema = var_db_name and table_name = @table_name ) THEN
            IF NOT EXISTS (SELECT * FROM information_schema.columns WHERE table_schema = var_db_name and table_name = @table_name AND column_name = @beforeColumn ) THEN
                set @statement = CONCAT('alter table ',var_db_name, '.',@table_name, ' add column ', @beforeColumn,' ',column_type,' DEFAULT NULL COMMENT "',@beforeColumnComment,'"');
                prepare STMT from @statement;
                execute STMT;
            END IF;
        END IF;

        -- 增加 conf_scaling_task 表的列：enableAfterstartScript
        IF EXISTS (SELECT * FROM information_schema.columns WHERE table_schema = var_db_name and table_name = @table_name ) THEN
            IF NOT EXISTS (SELECT * FROM information_schema.columns WHERE table_schema = var_db_name and table_name = @table_name AND column_name = @afterColumn ) THEN
                set @statement = CONCAT('alter table ',var_db_name, '.',@table_name, ' add column ', @afterColumn,' ',column_type,' DEFAULT NULL COMMENT "',@afterColumnComment,'"');
                prepare STMT from @statement;
                execute STMT;
            END IF;
        END IF;

        -- 增加conf_group_elastic_scaling_rule表列： enableAfterstartScript
        IF EXISTS (SELECT * FROM information_schema.columns WHERE table_schema = var_db_name and table_name = @ruleTableName ) THEN
            IF NOT EXISTS (SELECT * FROM information_schema.columns WHERE table_schema = var_db_name and table_name = @ruleTableName AND column_name = @beforeColumn ) THEN
                set @statement = CONCAT('alter table ',var_db_name, '.',@ruleTableName, ' add column ', @beforeColumn,' ',column_type,' DEFAULT NULL COMMENT "',@beforeColumnComment,'"');
                prepare STMT from @statement;
                execute STMT;
            END IF;
        END IF;

        -- 增加conf_group_elastic_scaling_rule表列： enableAfterstartScript
        IF EXISTS (SELECT * FROM information_schema.columns WHERE table_schema = var_db_name and table_name = @ruleTableName ) THEN
            IF NOT EXISTS (SELECT * FROM information_schema.columns WHERE table_schema = var_db_name and table_name = @ruleTableName AND column_name = @afterColumn ) THEN
                set @statement = CONCAT('alter table ',var_db_name, '.',@ruleTableName, ' add column ', @afterColumn,' ',column_type,' DEFAULT NULL COMMENT "',@afterColumnComment,'"');
                prepare STMT from @statement;
                execute STMT;
            END IF;
        END IF;

        set table_count = table_count - 1;
    end while;
END //
DELIMITER ;
-- 执行存储过程
call schema_change();
-- 删除存储过程
drop procedure if exists schema_change;