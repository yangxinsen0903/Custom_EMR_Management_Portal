
INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'spring.datasource.url',avalue,'sdp-vmsr','test','master','db' from config_detail
where application='sdp-compose' and akey='spring.datasource.url'
  and not exists (select 1 from config_detail where akey='spring.datasource.url' and application='sdp-vmsr');

INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'spring.datasource.username',avalue,'sdp-vmsr','test','master','db' from config_detail
where application='sdp-compose' and akey='spring.datasource.username'
  and not exists (select 1 from config_detail where akey='spring.datasource.username' and application='sdp-vmsr');

INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'spring.datasource.password',avalue,'sdp-vmsr','test','master','db' from config_detail
where application='sdp-compose' and akey='spring.datasource.password'
  and not exists (select 1 from config_detail where akey='spring.datasource.password' and application='sdp-vmsr');

INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'spring.datasource.tableconfig','[]','sdp-vmsr','test','master','db' from config_detail
where application='sdp-compose' and akey='spring.datasource.tableconfig'
  and not exists (select 1 from config_detail where akey='spring.datasource.tableconfig' and application='sdp-vmsr');

INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'mredis.lock.port',avalue,'sdp-vmsr','test','master','db' from config_detail
where application='sdp-compose' and akey='mredis.lock.port'
  and not exists (select 1 from config_detail where akey='mredis.lock.port' and application='sdp-vmsr');

INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'mredis.lock.address',avalue,'sdp-vmsr','test','master','db' from config_detail
where application='sdp-compose' and akey='mredis.lock.address'
  and not exists (select 1 from config_detail where akey='mredis.lock.address' and application='sdp-vmsr');

INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'mredis.lock.password',avalue,'sdp-vmsr','test','master','db' from config_detail
where application='sdp-compose' and akey='mredis.lock.password'
  and not exists (select 1 from config_detail where akey='mredis.lock.password' and application='sdp-vmsr');

INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'mredis.usessl',avalue,'sdp-vmsr','test','master','db' from config_detail
where application='sdp-compose' and akey='mredis.usessl'
  and not exists (select 1 from config_detail where akey='mredis.usessl' and application='sdp-vmsr');


INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'mredis.spot.port',avalue,'sdp-vmsr','test','master','db' from config_detail
where application='sdp-compose' and akey='mredis.spot.port'
  and not exists (select 1 from config_detail where akey='mredis.lock.port' and application='sdp-vmsr');

INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'mredis.spot.address',avalue,'sdp-vmsr','test','master','db' from config_detail
where application='sdp-compose' and akey='mredis.spot.address'
  and not exists (select 1 from config_detail where akey='mredis.lock.address' and application='sdp-vmsr');

INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'mredis.spot.password',avalue,'sdp-vmsr','test','master','db' from config_detail
where application='sdp-compose' and akey='mredis.spot.password'
  and not exists (select 1 from config_detail where akey='mredis.spot.password' and application='sdp-vmsr');

INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'mredis.spot.db',avalue,'sdp-vmsr','test','master','db' from config_detail
where application='sdp-compose' and akey='mredis.spot.db'
  and not exists (select 1 from config_detail where akey='mredis.spot.db' and application='sdp-vmsr');

INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'mredis.spot.usessl',avalue,'sdp-vmsr','test','master','db' from config_detail
where application='sdp-compose' and akey='mredis.spot.usessl'
  and not exists (select 1 from config_detail where akey='mredis.spot.usessl' and application='sdp-vmsr');



