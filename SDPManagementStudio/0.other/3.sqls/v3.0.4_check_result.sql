
-- 更新镜像
select '3.0.4' as version, 'base_images' as table_name, if(count(*)=1, true, false) as check_result,'insert' as action
from base_images
where img_id ='a9009812d7f14c669e2307254ebdd875' and os_image_id like '%1.0.1442%';
