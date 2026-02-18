-- 设置GangliaServer默认启动
INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'sdp.ganglia.install.enable','1','sdp-compose','test','master','db'
where not exists
( select 1 from config_detail where  akey = 'sdp.ganglia.install.enable' and application = 'sdp-compose' );