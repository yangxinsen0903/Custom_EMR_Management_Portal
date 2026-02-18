
-- 更新镜像
select '2.0.5' as version, 'base_images' as table_name, if(count(*)=1, true, false) as check_result,'insert' as action
from base_images
where img_id ='4112bf6b877f40139d3932d003856ed9'
union all
select '2.0.5' as version, 'base_image_scripts' as table_name, if(count(*)=5, true, false) as check_result,'insert' as action
from base_image_scripts
where img_id ='4112bf6b877f40139d3932d003856ed9'
union all
select '2.0.5' as version, 'base_release_vm_img' as table_name, if(count(*)=4, true, false) as check_result,'update' as action
from base_release_vm_img
where img_id ='4112bf6b877f40139d3932d003856ed9';