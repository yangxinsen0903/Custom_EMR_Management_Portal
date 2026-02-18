
-- 更新镜像
select '3.0.1' as version, 'base_images' as table_name, if(count(*)=1, true, false) as check_result,'insert' as action
from base_images
where img_id ='a0a35cab595d4e6d9b9daa4de91b62e8'
union all
select '3.0.1' as version, 'base_image_scripts' as table_name, if(count(*)=8, true, false) as check_result,'insert' as action
from base_image_scripts
where img_id ='a0a35cab595d4e6d9b9daa4de91b62e8'
union all
select '3.0.1' as version, 'base_release_vm_img' as table_name, if(count(*)=4, true, false) as check_result,'update' as action
from base_release_vm_img
where img_id ='a0a35cab595d4e6d9b9daa4de91b62e8';
