INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'metric.expire.time','300','sdp-scale','test','master','db'
where not exists (select 1 from config_detail where akey='metric.expire.time' and application='sdp-scale');

INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'metric.active.check.expire.time','*/5 * * * * ?','sdp-scale','test','master','db'
where not exists (select 1 from config_detail where akey='metric.active.check.expire.time' and application='sdp-scale');