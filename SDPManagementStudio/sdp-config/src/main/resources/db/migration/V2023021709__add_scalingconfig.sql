INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'spot.scale.manage.task.time','*/30 * * * * ?','sdp-spot','test','master','db'
where not exists (select 1 from config_detail where akey='spot.scale.manage.task.time' and application='sdp-spot')
limit 1;

INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'spring.datasource.url',avalue,'sdp-spot','test','master','db' from config_detail
where application='sdp-compose' and akey='spring.datasource.url'
and not exists (select 1 from config_detail where akey='spring.datasource.url' and application='sdp-spot');

INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'spring.datasource.username',avalue,'sdp-spot','test','master','db' from config_detail
where application='sdp-compose' and akey='spring.datasource.username'
and not exists (select 1 from config_detail where akey='spring.datasource.username' and application='sdp-spot');

INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'spring.datasource.password',avalue,'sdp-spot','test','master','db' from config_detail
where application='sdp-compose' and akey='spring.datasource.password'
and not exists (select 1 from config_detail where akey='spring.datasource.password' and application='sdp-spot');

INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'spring.datasource.tableconfig',avalue,'sdp-spot','test','master','db' from config_detail
where application='sdp-compose' and akey='spring.datasource.tableconfig'
and not exists (select 1 from config_detail where akey='spring.datasource.tableconfig' and application='sdp-spot');

INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'mredis.lock.port',avalue,'sdp-spot','test','master','db' from config_detail
where application='sdp-compose' and akey='mredis.lock.port'
and not exists (select 1 from config_detail where akey='mredis.lock.port' and application='sdp-spot');

INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'mredis.lock.address',avalue,'sdp-spot','test','master','db' from config_detail
where application='sdp-compose' and akey='mredis.lock.address'
and not exists (select 1 from config_detail where akey='mredis.lock.address' and application='sdp-spot');

INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'mredis.lock.password',avalue,'sdp-spot','test','master','db' from config_detail
where application='sdp-compose' and akey='mredis.lock.password'
and not exists (select 1 from config_detail where akey='mredis.lock.password' and application='sdp-spot');

INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'mredis.usessl',avalue,'sdp-spot','test','master','db' from config_detail
where application='sdp-compose' and akey='mredis.usessl'
and not exists (select 1 from config_detail where akey='mredis.usessl' and application='sdp-spot');