INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'metric.collect.server.expire.time','300','sdp-scale','test','master','db'
where not exists (select 1 from config_detail where akey='metric.collect.server.expire.time' and application='sdp-scale');

INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'hadoop.jmx.api.port','8088','sdp-scale','test','master','db'
where not exists (select 1 from config_detail where akey='hadoop.jmx.api.port' and application='sdp-scale');

INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'metric.collect.wait.time','60','sdp-scale','test','master','db'
where not exists (select 1 from config_detail where akey='metric.collect.wait.time' and application='sdp-scale');

INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'spring.datasource.url',avalue,'sdp-scale','test','master','db' from config_detail
where application='sdp-compose' and akey='spring.datasource.url'
and not exists (select 1 from config_detail where akey='spring.datasource.url' and application='sdp-scale');

INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'spring.datasource.username',avalue,'sdp-scale','test','master','db' from config_detail
where application='sdp-compose' and akey='spring.datasource.username'
and not exists (select 1 from config_detail where akey='spring.datasource.username' and application='sdp-scale');

INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'spring.datasource.password',avalue,'sdp-scale','test','master','db' from config_detail
where application='sdp-compose' and akey='spring.datasource.password'
and not exists (select 1 from config_detail where akey='spring.datasource.password' and application='sdp-scale');

INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'spring.datasource.tableconfig',avalue,'sdp-scale','test','master','db' from config_detail
where application='sdp-compose' and akey='spring.datasource.tableconfig'
and not exists (select 1 from config_detail where akey='spring.datasource.tableconfig' and application='sdp-scale');

INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'mredis.lock.port',avalue,'sdp-scale','test','master','db' from config_detail
where application='sdp-compose' and akey='mredis.lock.port'
and not exists (select 1 from config_detail where akey='mredis.lock.port' and application='sdp-scale');

INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'mredis.lock.address',avalue,'sdp-scale','test','master','db' from config_detail
where application='sdp-compose' and akey='mredis.lock.address'
and not exists (select 1 from config_detail where akey='mredis.lock.address' and application='sdp-scale');

INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'mredis.lock.password',avalue,'sdp-scale','test','master','db' from config_detail
where application='sdp-compose' and akey='mredis.lock.password'
and not exists (select 1 from config_detail where akey='mredis.lock.password' and application='sdp-scale');

INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'mredis.usessl',avalue,'sdp-scale','test','master','db' from config_detail
where application='sdp-compose' and akey='mredis.usessl'
and not exists (select 1 from config_detail where akey='mredis.usessl' and application='sdp-scale');