-- Dev环境/SIT环境/美国站的 Region配置更新
select '2.0.2' as version, 'conf_cluster' as table_name, if(count(*)=0, true, false) as check_result,'update' as action
from conf_cluster
where region is null
union all
select '2.0.2' as version, 'config_detail' as table_name, if(count(*)=1, true, false) as check_result,'update' as action
from config_detail
where akey = 'sdp.region' and avalue = 'uswest3';
