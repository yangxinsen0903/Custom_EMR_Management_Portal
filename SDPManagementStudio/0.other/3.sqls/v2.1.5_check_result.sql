
-- 更新中央站Blob账号
select '2.1.5' as version, 'config_detail' as table_name, if(count(*)=1, true, false) as check_result,'update' as action
from config_detail
where avalue = 'cep1prod1abcops.blob.core.windows.net' and akey = 'sdp.wgetpath'
union all
select '2.1.5' as version, 'base_script' as table_name, if(count(*)>0, true, false) as check_result,'update' as action
from base_script
where blob_path like '%cep1prod1abcops%';
