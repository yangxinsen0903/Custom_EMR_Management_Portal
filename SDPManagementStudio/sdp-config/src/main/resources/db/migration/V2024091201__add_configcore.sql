
INSERT INTO config_core (akey,avalue,application,profile,label,mwtype)
select 'hostname.domain','wu2dns.shein.com','core','test','master','db' from config_core
where not exists (select 1 from config_core where akey='hostname.domain' ) limit 1;
