ALTER TABLE info_vm_statement_result MODIFY COLUMN subnet varchar(512);
ALTER TABLE info_vm_statement_result MODIFY COLUMN nic_id varchar(512);
ALTER TABLE info_vm_statement_result MODIFY COLUMN sku varchar(128);
ALTER TABLE info_vm_statement_result MODIFY COLUMN vm_name varchar(256);

ALTER TABLE info_vm_statement_item MODIFY COLUMN subnet varchar(512);
ALTER TABLE info_vm_statement_item MODIFY COLUMN nic_id varchar(512);
ALTER TABLE info_vm_statement_item MODIFY COLUMN sku varchar(128);
ALTER TABLE info_vm_statement_item MODIFY COLUMN vm_name varchar(256);