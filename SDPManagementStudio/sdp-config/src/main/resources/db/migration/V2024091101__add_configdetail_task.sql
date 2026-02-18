
INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'azure.delete.task.time','0 0 0/2 * * ?','sdp-task','test','master','db' from config_detail
where not exists (select 1 from config_detail where akey='azure.delete.task.time' and application='sdp-task') limit 1;
