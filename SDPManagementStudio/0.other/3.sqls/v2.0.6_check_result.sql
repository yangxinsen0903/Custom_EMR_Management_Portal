-- 更新更新旧集群镜像参数
select '2.0.6' as version, 'base_image_scripts' as table_name, if(count(*)=1, true, false) as check_result,'update' as action
from base_image_scripts
where img_script_id = 'e2a719bea06c11ed922d6045bdc792d8' and img_id = '8596cf8ea06c11ed922d6045bdc792d8' and extra_vars like '%miclientid={miclientid}%';
