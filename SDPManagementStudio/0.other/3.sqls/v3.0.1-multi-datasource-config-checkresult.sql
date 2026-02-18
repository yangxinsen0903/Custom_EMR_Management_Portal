-- 检查数据插入是否正确
select '3.0.1' as version, 'config_detail' as table_name, if(count(*)=2, true, false) as check_result,'insert-multds' as action
from config_detail cd 
where akey = 'spring.datasource.multconfig'
and application in ( 'sdp-compose', 'sdp-admin')
union
select '3.0.1' as version, 'config_detail' as table_name, if(count(*)=2, true, false) as check_result,'insert-table' as action
from config_detail cd 
where akey = 'spring.datasource.tableconfig'
and application in ( 'sdp-compose', 'sdp-admin');