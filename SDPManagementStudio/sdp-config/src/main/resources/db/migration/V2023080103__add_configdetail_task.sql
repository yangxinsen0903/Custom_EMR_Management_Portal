
INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'mredis.spot.port',avalue,'sdp-task','test','master','db' from config_detail
where application='sdp-compose' and akey='mredis.spot.port'
  and not exists (select 1 from config_detail where akey='mredis.lock.port' and application='sdp-task');

INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'mredis.spot.address',avalue,'sdp-task','test','master','db' from config_detail
where application='sdp-compose' and akey='mredis.spot.address'
  and not exists (select 1 from config_detail where akey='mredis.lock.address' and application='sdp-task');

INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'mredis.spot.password',avalue,'sdp-task','test','master','db' from config_detail
where application='sdp-compose' and akey='mredis.spot.password'
  and not exists (select 1 from config_detail where akey='mredis.spot.password' and application='sdp-task');

INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'mredis.spot.db',avalue,'sdp-task','test','master','db' from config_detail
where application='sdp-compose' and akey='mredis.spot.db'
  and not exists (select 1 from config_detail where akey='mredis.spot.db' and application='sdp-task');

INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'mredis.spot.usessl',avalue,'sdp-task','test','master','db' from config_detail
where application='sdp-compose' and akey='mredis.spot.usessl'
  and not exists (select 1 from config_detail where akey='mredis.spot.usessl' and application='sdp-task');
