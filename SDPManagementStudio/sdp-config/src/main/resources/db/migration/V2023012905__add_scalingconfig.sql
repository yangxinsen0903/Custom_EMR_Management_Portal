INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'scale.task.thread.pool.size','50','sdp-scale','test','master','db'
where not exists (select 1 from config_detail where akey='scale.task.thread.pool.size' and application='sdp-scale');