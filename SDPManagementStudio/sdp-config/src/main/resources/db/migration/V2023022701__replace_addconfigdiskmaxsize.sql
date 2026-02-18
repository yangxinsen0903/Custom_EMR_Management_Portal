-- 删除
delete from config_detail where akey='sdp.datadisksize.max' and application='sdp-admin';
-- 新增记录
INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype) VALUES
    ('sdp.datadisksize.max','4096','sdp-admin','test','master','db');
