delete from config_core where akey='sdp.scripts.blobpath';

INSERT INTO config_core (akey,avalue,application,profile,label,mwtype)
select 'sdp.scripts.blobpath',avalue,'core','test','master','db' from config_detail
where  akey='sdp.wgetpath' and application='sdp-compose';