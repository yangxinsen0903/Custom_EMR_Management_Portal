
-- 更新镜像
select '2.0.8' as version, 'base_images' as table_name, if(count(*)=1, true, false) as check_result,'insert' as action
from base_images
where img_id ='e0da6c4d69934a9db13ede3a26ee9fc3'
union all
select '2.0.8' as version, 'base_image_scripts' as table_name, if(count(*)=6, true, false) as check_result,'insert' as action
from base_image_scripts
where img_id ='e0da6c4d69934a9db13ede3a26ee9fc3'
union all
select '2.0.8' as version, 'base_release_vm_img' as table_name, if(count(*)=4, true, false) as check_result,'update' as action
from base_release_vm_img
where img_id ='e0da6c4d69934a9db13ede3a26ee9fc3';
