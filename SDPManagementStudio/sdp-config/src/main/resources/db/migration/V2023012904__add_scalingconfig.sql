INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'scale.active.check.expire.time','*/30 * * * * ?','sdp-scale','test','master','db'
where not exists (select 1 from config_detail where akey='scale.active.check.expire.time' and application='sdp-scale');

INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'metric.collect.task.time','*/5 * * * * ?','sdp-scale','test','master','db'
where not exists (select 1 from config_detail where akey='metric.collect.task.time' and application='sdp-scale');