delete from config_core where akey='rmapi.request.url';

INSERT INTO config_core (akey,avalue,application,profile,label,mwtype)
select 'rmapi.request.url',avalue,'core','test','master','db' from config_detail
where  akey='azure.request.url' and application='sdp-compose';