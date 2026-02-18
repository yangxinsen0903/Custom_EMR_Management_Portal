
-- 更新镜像
select '2.1.3' as version, 'base_images' as table_name, if(count(*)=1, true, false) as check_result,'insert' as action
from base_images
where img_id ='a02a9a7a1eed435e8816684be3c8392c'
union all
select '2.0.9' as version, 'base_image_scripts' as table_name, if(count(*)=8, true, false) as check_result,'insert' as action
from base_image_scripts
where img_id ='a02a9a7a1eed435e8816684be3c8392c'
union all
select '2.0.9' as version, 'base_release_vm_img' as table_name, if(count(*)=4, true, false) as check_result,'update' as action
from base_release_vm_img
where img_id ='a02a9a7a1eed435e8816684be3c8392c';
