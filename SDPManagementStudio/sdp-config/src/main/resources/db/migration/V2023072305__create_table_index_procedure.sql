
DROP PROCEDURE IF EXISTS create_index;
DELIMITER $
CREATE PROCEDURE create_index(IN target_table_name VARCHAR(100),
    IN target_index_name VARCHAR(100),
    IN target_column_name VARCHAR(100))
BEGIN
    DECLARE  target_database VARCHAR(100);
    SELECT DATABASE() INTO target_database;


    IF NOT EXISTS (SELECT * FROM information_schema.statistics WHERE table_schema = target_database AND table_name = target_table_name AND index_name = target_index_name) THEN
        set @statement = CONCAT('CREATE INDEX  ',target_index_name,' ON ',target_table_name,target_column_name);
        PREPARE STMT FROM @statement;
        EXECUTE STMT;
    END IF;
END;
$
DELIMITER ;