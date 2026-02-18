
-- 更新镜像
select '3.0' as version, 'base_images' as table_name, if(count(*)=1, true, false) as check_result,'insert' as action
from base_images
where img_id ='03cf41d17abd45d889f0da3d76da61e0'
union all
select '3.0' as version, 'base_image_scripts' as table_name, if(count(*)=8, true, false) as check_result,'insert' as action
from base_image_scripts
where img_id ='03cf41d17abd45d889f0da3d76da61e0'
union all
select '3.0' as version, 'base_release_vm_img' as table_name, if(count(*)=4, true, false) as check_result,'update' as action
from base_release_vm_img
where img_id ='03cf41d17abd45d889f0da3d76da61e0';
