-- 校验手动销毁生产环境中的主机的更新结果是否正确
select '2.0.7' as version, 'info_cluster_vm' as table_name, if(count(*)=15, true, false) as check_result,'update' as action
from info_cluster_vm
where cluster_id = 'f1778847-bc19-4249-91b5-9eecd28ec4bc' and  host_name in (
    'prod-dw-dw-l-002-tsk-0228.sdp.azure.com',
    'prod-dw-dw-l-002-tsk-0229.sdp.azure.com',
    'prod-dw-dw-l-002-tsk-0238.sdp.azure.com',
    'prod-dw-dw-l-002-tsk-0242.sdp.azure.com',
    'prod-dw-dw-l-002-tsk-0243.sdp.azure.com',
    'prod-dw-dw-l-002-tsk-0244.sdp.azure.com',
    'prod-dw-dw-l-002-tsk-0250.sdp.azure.com',
    'prod-dw-dw-l-002-tsk-0252.sdp.azure.com',
    'prod-dw-dw-l-002-tsk-0256.sdp.azure.com',
    'prod-dw-dw-l-002-tsk-0262.sdp.azure.com',
    'prod-dw-dw-l-002-tsk-0264.sdp.azure.com',
    'prod-dw-dw-l-002-tsk-0265.sdp.azure.com',
    'prod-dw-dw-l-002-tsk-0266.sdp.azure.com',
    'prod-dw-dw-l-002-tsk-0270.sdp.azure.com',
    'prod-dw-dw-l-002-tsk-0278.sdp.azure.com'
) and state  = -1
union all
select '2.0.7' as version, 'info_cluster_vm' as table_name, if(count(*)=30, true, false) as check_result,'update' as action
from info_cluster_vm
where cluster_id = '1ac6b01b-274d-42f9-9e79-db475697893a' and  host_name in (
    'prod-dw-hour-l-003-tsk-0073.sdp.azure.com',
    'prod-dw-hour-l-003-tsk-0074.sdp.azure.com',
    'prod-dw-hour-l-003-tsk-0076.sdp.azure.com',
    'prod-dw-hour-l-003-tsk-0079.sdp.azure.com',
    'prod-dw-hour-l-003-tsk-0083.sdp.azure.com',
    'prod-dw-hour-l-003-tsk-0084.sdp.azure.com',
    'prod-dw-hour-l-003-tsk-0085.sdp.azure.com',
    'prod-dw-hour-l-003-tsk-0087.sdp.azure.com',
    'prod-dw-hour-l-003-tsk-0088.sdp.azure.com',
    'prod-dw-hour-l-003-tsk-0089.sdp.azure.com',
    'prod-dw-hour-l-003-tsk-0091.sdp.azure.com',
    'prod-dw-hour-l-003-tsk-0092.sdp.azure.com',
    'prod-dw-hour-l-003-tsk-0097.sdp.azure.com',
    'prod-dw-hour-l-003-tsk-0098.sdp.azure.com',
    'prod-dw-hour-l-003-tsk-0099.sdp.azure.com',
    'prod-dw-hour-l-003-tsk-0100.sdp.azure.com',
    'prod-dw-hour-l-003-tsk-0102.sdp.azure.com',
    'prod-dw-hour-l-003-tsk-0105.sdp.azure.com',
    'prod-dw-hour-l-003-tsk-0107.sdp.azure.com',
    'prod-dw-hour-l-003-tsk-0108.sdp.azure.com',
    'prod-dw-hour-l-003-tsk-0114.sdp.azure.com',
    'prod-dw-hour-l-003-tsk-0120.sdp.azure.com',
    'prod-dw-hour-l-003-tsk-0121.sdp.azure.com',
    'prod-dw-hour-l-003-tsk-0123.sdp.azure.com',
    'prod-dw-hour-l-003-tsk-0124.sdp.azure.com',
    'prod-dw-hour-l-003-tsk-0129.sdp.azure.com',
    'prod-dw-hour-l-003-tsk-0131.sdp.azure.com',
    'prod-dw-hour-l-003-tsk-0134.sdp.azure.com',
    'prod-dw-hour-l-003-tsk-0135.sdp.azure.com',
    'prod-dw-hour-l-003-tsk-0138.sdp.azure.com'
) and state  = -1;

