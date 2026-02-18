-- 镜像更新
update base_release_vm_img set os_imageid = '/subscriptions/9b88bd64-d315-48de-96bc-83051ed25fdc/resourceGroups/rg-sdp-dev-test/providers/Microsoft.Compute/galleries/sig_sdp_images/images/sunbox-ubuntu/versions/1.17.2'
where release_version = 'SDP-1.0' and vm_role in ('ambari', 'core', 'master', 'task');
