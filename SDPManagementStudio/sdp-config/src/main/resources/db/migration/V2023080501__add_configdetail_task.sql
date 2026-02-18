delete from config_detail where application='sdp-task' and akey='vm.statement.task.time';

INSERT INTO config_core (akey,avalue,application,profile,label,mwtype) value ('vm.statement.task.time','0 0/10 * * * ?','sdp-task','test','master','task');
