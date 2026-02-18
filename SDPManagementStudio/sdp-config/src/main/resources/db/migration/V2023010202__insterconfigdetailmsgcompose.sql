delete from config_detail where akey='message.compose' and application='sdp-compose';
INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype) VALUES
    ('message.compose','servicebus','sdp-compose','test','master','db');