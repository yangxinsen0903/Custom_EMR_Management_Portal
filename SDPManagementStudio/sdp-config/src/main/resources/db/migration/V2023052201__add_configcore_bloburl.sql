delete from config_core where akey='log.zip.upload.url';

INSERT INTO config_core (akey,avalue,application,profile,label,mwtype)
select 'log.zip.upload.url',avalue,'core','test','master','db' from config_detail
where  akey='azure.request.url' and application='sdp-compose';