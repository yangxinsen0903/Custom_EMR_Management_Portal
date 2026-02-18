select '3.0.4' as version, 'conf_cluster_vm' as table_name, if(count(*)=0, true, false) as check_result,'update' as action
from conf_cluster_vm
where os_image_type = 'CustomImage'
  and cluster_id in ('870ff4bf-c92a-49cf-b2d4-23c868acdc75',
                     '7bb0b526-b230-4093-bf43-da8aba87ba50',
                     'd75efc15-e6ef-401a-a2fa-5a94d31318fa',
                     '2e4f25d5-779c-4f25-9ad9-e857ea89dd6c',
                     'fd1d01c1-2b42-4411-896d-60dbc30ca79f',
                     'a5f4a5f2-4948-431e-94d6-24b3b860fb90')
  and os_imageid = '/subscriptions/0fb499ac-5979-4b63-b48c-3b5dc1341c50/resourceGroups/rg-us-p1/providers/Microsoft.Compute/galleries/sdp_us_gallery/images/sdp-ubuntu/versions/3.0.3';
