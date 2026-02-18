
-- 更新镜像
select '2.0.9' as version, 'base_images' as table_name, if(count(*)=1, true, false) as check_result,'insert' as action
from base_images
where img_id ='c3a0ffd8c02841fe88e4e068868fa0c4'
union all
select '2.0.9' as version, 'base_image_scripts' as table_name, if(count(*)=6, true, false) as check_result,'insert' as action
from base_image_scripts
where img_id ='c3a0ffd8c02841fe88e4e068868fa0c4'
union all
select '2.0.9' as version, 'base_release_vm_img' as table_name, if(count(*)=4, true, false) as check_result,'update' as action
from base_release_vm_img
where img_id ='c3a0ffd8c02841fe88e4e068868fa0c4';
