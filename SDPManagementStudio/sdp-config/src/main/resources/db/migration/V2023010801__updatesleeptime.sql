delete from config_detail where akey='install.waitingtime' and application='sdp-compose';
INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype) VALUES
    ('install.waitingtime','30','sdp-compose','test','master','db');