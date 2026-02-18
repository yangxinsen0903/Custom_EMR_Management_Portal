-- 设置GangliaServer默认关闭
update config_detail set avalue = '0'
where  akey = 'sdp.ganglia.install.enable' and application = 'sdp-compose';