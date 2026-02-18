INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'mredis.spot.port',avalue,'sdp-compose','test','master','db' from config_detail
where application='sdp-spot' and akey='mredis.spot.port'
and not exists (select 1 from config_detail where akey='mredis.spot.port' and application='sdp-compose');

INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'mredis.spot.address',avalue,'sdp-compose','test','master','db' from config_detail
where application='sdp-spot' and akey='mredis.spot.address'
and not exists (select 1 from config_detail where akey='mredis.spot.address' and application='sdp-compose');

INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'mredis.spot.password',avalue,'sdp-compose','test','master','db' from config_detail
where application='sdp-spot' and akey='mredis.spot.password'
and not exists (select 1 from config_detail where akey='mredis.spot.password' and application='sdp-compose');

INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'mredis.spot.db',avalue,'sdp-compose','test','master','db' from config_detail
where application='sdp-spot' and akey='mredis.spot.db'
and not exists (select 1 from config_detail where akey='mredis.spot.port' and application='sdp-compose');

INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'mredis.spot.usessl',avalue,'sdp-compose','test','master','db' from config_detail
where application='sdp-spot' and akey='mredis.spot.usessl'
and not exists (select 1 from config_detail where akey='mredis.spot.usessl' and application='sdp-compose');


INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'mredis.spot.port',avalue,'sdp-scale','test','master','db' from config_detail
where application='sdp-spot' and akey='mredis.spot.port'
and not exists (select 1 from config_detail where akey='mredis.spot.port' and application='sdp-scale');

INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'mredis.spot.address',avalue,'sdp-scale','test','master','db' from config_detail
where application='sdp-spot' and akey='mredis.spot.address'
and not exists (select 1 from config_detail where akey='mredis.spot.address' and application='sdp-scale');

INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'mredis.spot.password',avalue,'sdp-scale','test','master','db' from config_detail
where application='sdp-spot' and akey='mredis.spot.password'
and not exists (select 1 from config_detail where akey='mredis.spot.password' and application='sdp-scale');

INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'mredis.spot.db',avalue,'sdp-scale','test','master','db' from config_detail
where application='sdp-spot' and akey='mredis.spot.db'
and not exists (select 1 from config_detail where akey='mredis.spot.port' and application='sdp-scale');

INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'mredis.spot.usessl',avalue,'sdp-scale','test','master','db' from config_detail
where application='sdp-spot' and akey='mredis.spot.usessl'
and not exists (select 1 from config_detail where akey='mredis.spot.usessl' and application='sdp-scale');
