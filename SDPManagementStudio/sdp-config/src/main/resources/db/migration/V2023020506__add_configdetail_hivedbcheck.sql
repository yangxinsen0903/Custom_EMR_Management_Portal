delete from config_detail where akey='hive.db.check' and application='sdp-admin';
INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype) VALUES
    ('hive.db.check','true','sdp-admin','test','master','db');
