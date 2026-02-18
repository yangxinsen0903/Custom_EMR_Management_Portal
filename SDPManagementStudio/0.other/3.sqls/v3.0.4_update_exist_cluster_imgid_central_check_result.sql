select '3.0.4' as version, 'conf_cluster_vm' as table_name, if(count(*)=0, true, false) as check_result,'update' as action
from conf_cluster_vm
where os_image_type = 'CustomImage'
  and cluster_id in ('ee5712ff-b61a-415e-9697-a11a9eaa60a1',
                     'af9c4af4-f3a2-41d1-9a2d-7031ba55010f',
                     '8e159d24-b925-4b6a-9280-60cafdaac4bd',
                     '8e5e1655-fd5b-460d-b27d-55e588913e71',
                     '2e294d36-5f89-4a74-9c3b-ec3d8ea0e92b',
                     'ce5534b9-c67b-4b01-8d72-a3dc4837d6ee',
                     'a6fd1a3f-a0d8-455f-ad12-ca66993e2153',
                     '320aaa9e-229e-46a3-aad1-78ac674293a4',
                     '97e35eb0-4dd6-46c1-89f0-a2797cd34b1e',
                     '3935f098-a0c1-43b4-8ec1-f0b648026368',
                     'b849ba0b-5cc1-4d35-b219-5e5d86aae381')
  and os_imageid = '/subscriptions/3e2aa15f-923a-460c-90a0-9e533d7ab2df/resourceGroups/rg-central-p1/providers/Microsoft.Compute/galleries/sdp_central_gallery/images/sdp-ubuntu/versions/3.0.3';
