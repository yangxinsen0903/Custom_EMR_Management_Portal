
update sdpms.config_detail set avalue='hadoop' where akey ='vm.username';

-- 在创建集群时，配置Ambari组件自动重启的开关
delete from sdpms.config_detail where akey = 'ambari.settings.autostart' and application  = 'sdp-compose';
INSERT INTO sdpms.config_detail (akey,avalue,application,profile,label,mwtype) VALUES
    ('ambari.settings.autostart','0','sdp-compose','test','master','db');
