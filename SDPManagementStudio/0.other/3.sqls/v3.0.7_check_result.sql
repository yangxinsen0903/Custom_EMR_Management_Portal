-- 更新镜像
select '3.0.7' as version, 'base_images' as table_name, if(count(*)=1, true, false) as check_result,'insert' as action
from base_images
where img_id ='e5fba7b9e71f4ab6a8f9865e5e6366a3'
union all
select '3.0.7' as version, 'base_image_scripts' as table_name, if(count(*)=8, true, false) as check_result,'insert' as action
from base_image_scripts
where img_id ='e5fba7b9e71f4ab6a8f9865e5e6366a3'
union all
select '3.0.7' as version, 'base_release_vm_img' as table_name, if(count(*)=4, true, false) as check_result,'update' as action
from base_release_vm_img
where img_id ='e5fba7b9e71f4ab6a8f9865e5e6366a3';
