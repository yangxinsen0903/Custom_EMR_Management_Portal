
INSERT INTO base_images (img_id,os_image_id,os_image_type,os_version,created_time,createdby) VALUES
('8596cf8ea06c11ed922d6045bdc792d8','/subscriptions/9b88bd64-d315-48de-96bc-83051ed25fdc/resourceGroups/rg-sunbox-sdp-build/providers/Microsoft.Compute/galleries/sig_sunbox_sdp_images/images/sunbox-sdp/versions/1.0.888','CustomImage','ubuntu 18.04','2023-01-30 11:45:20','niyang');


INSERT INTO base_image_scripts (img_script_id,img_id,script_name,run_timing,playbook_uri,script_file_uri,extra_vars,sort_no,createdby,created_time) VALUES
('d9fa29c2a06c11ed922d6045bdc792d8','8596cf8ea06c11ed922d6045bdc792d8','安装AmbariServer','install_ambari_server','https://{wgetpath}/sunbox3/shell/2.0.1/ambari.yaml','https://{wgetpath}/sunbox3/shell/2.0.1/ambari-agent.sh,https://{wgetpath}/sunbox3/shell/2.0.1/ambari-server.sh,https://{wgetpath}/sunbox3/shell/2.0.1/initialize.sh,https://{wgetpath}/sunbox3/shell/2.0.1/mountnew.sh,https://{wgetpath}/sunbox3/shell/2.0.1/ganglia-agent.sh,https://{wgetpath}/sunbox3/shell/2.0.1/ganglia-server.sh','-e "type=server host={ambarihost} dburl={ambaridb} dbport={ambaridbport} dbname={ambaridbname} dbuser={dbuser} dbpass={dbpassword} domain={domain} wgetpath={wgetpath} clustername={clustername} logblob={logblob} username={username} clusterid={clusterid} ganglialist={ganglialist} installganglia={installganglia} zone={zone}"',0,'niyang','2023-01-29 15:27:29'),
('e2a719bea06c11ed922d6045bdc792d8','8596cf8ea06c11ed922d6045bdc792d8','安装AmbariAgent','install_ambari_agent','https://{wgetpath}/sunbox3/shell/2.0.1/ambari.yaml','https://{wgetpath}/sunbox3/shell/2.0.1/ambari-agent.sh,https://{wgetpath}/sunbox3/shell/2.0.1/ambari-server.sh,https://{wgetpath}/sunbox3/shell/2.0.1/initialize.sh,https://{wgetpath}/sunbox3/shell/2.0.1/mountnew.sh,https://{wgetpath}/sunbox3/shell/2.0.1/ganglia-agent.sh,https://{wgetpath}/sunbox3/shell/2.0.1/ganglia-server.sh','-e "type=agent host={ambarihost} dburl={ambaridb} dbport={ambaridbport} dbname={ambaridbname} dbuser={dbuser} dbpass={dbpassword} domain={domain} wgetpath={wgetpath} clustername={clustername} logblob={logblob} username={username} clusterid={clusterid} ganglialist={ganglialist} installganglia={installganglia} zone={zone}"',0,'niyang','2023-01-25 15:27:29'),
('b68dcb10a07711ed922d6045bdc792d8','8596cf8ea06c11ed922d6045bdc792d8','安装TezUI','install_tez_ui','https://{wgetpath}/sunbox3/shell/2.0.1/tezui.yaml','https://{wgetpath}/sunbox3/shell/2.0.1/tezui.sh','-e "tlhost={tlhost} rmhost={rmhost} username={username}"',0,'niyang','2023-01-25 15:27:29'),
('a57e680aa07811ed922d6045bdc792d8','8596cf8ea06c11ed922d6045bdc792d8','执行HDFS FSCK','run_hdfs_fsck','','https://{wgetpath}/sunbox3/shell/2.0.1/hdfs.sh','',0,'niyang','2023-01-25 15:27:29');

update base_release_vm_img
set os_imageid='/subscriptions/9b88bd64-d315-48de-96bc-83051ed25fdc/resourceGroups/rg-sunbox-sdp-build/providers/Microsoft.Compute/galleries/sig_sunbox_sdp_images/images/sunbox-sdp/versions/1.0.888',
img_id='8596cf8ea06c11ed922d6045bdc792d8' where release_version ='SDP-1.0';


