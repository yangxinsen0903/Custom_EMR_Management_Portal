
REPLACE INTO base_images
(img_id, os_image_id, os_image_type, os_version, created_time, createdby)
VALUES('a02a9a7a1eed435e8816684be3c8392c', '/subscriptions/9b88bd64-d315-48de-96bc-83051ed25fdc/resourceGroups/rg-sunbox-sdp-build/providers/Microsoft.Compute/galleries/sig_sunbox_sdp_images/images/sunbox-sdp/versions/1.0.1305', 'CustomImage', 'ubuntu 18.04', '2023-02-19 10:20:37', 'niyang');

REPLACE INTO base_image_scripts (img_script_id,img_id,script_name,run_timing,playbook_uri,script_file_uri,extra_vars,sort_no,createdby,created_time) VALUES
         ('540fbda517e84aa78b544cf0f050ef8e','a02a9a7a1eed435e8816684be3c8392c','安装TezUI','install_tez_ui','https://{wgetpath}/sunbox3/shell/2.0.4/tezui.yaml','https://{wgetpath}/sunbox3/shell/2.0.4/tezui.sh','-e "tlhost={tlhost} rmhost={rmhost} username={username}"',0,'niyang','2023-02-19 10:20:37'),
         ('f03af283b7834c2aab3c420c2843aa5d','a02a9a7a1eed435e8816684be3c8392c','清理GangliaData','clear_ganglia_data','https://{wgetpath}/sunbox3/shell/2.0.4/ganglia-agentdel.yaml','https://{wgetpath}/sunbox3/shell/2.0.4/ganglia-agentdel.sh','-e "username={username}  agentname={agentname}"',0,'niyang','2023-02-19 10:20:37'),
         ('89a01bf790d7495096cac8e016fb2e34','a02a9a7a1eed435e8816684be3c8392c','收集日志','collectLogs','https://{wgetpath}/sunbox3/shell/2.0.4/logandetc.yaml','https://{wgetpath}/sunbox3/shell/2.0.4/logandetc.sh','-e "logblob={logblob} clusterid={clusterid} miclientid={miclientid} logid={logid} username={username}"',0,'niyang','2023-02-14 05:55:18'),
         ('b27610d9165545a78b49fc4b37cf6eea','a02a9a7a1eed435e8816684be3c8392c','安装AmbariAgent','install_ambari_agent','https://{wgetpath}/sunbox3/shell/2.0.4/ambari.yaml','https://{wgetpath}/sunbox3/shell/2.0.4/ambari-agent.sh,https://{wgetpath}/sunbox3/shell/2.0.4/ambari-server.sh,https://{wgetpath}/sunbox3/shell/2.0.4/initialize.sh,https://{wgetpath}/sunbox3/shell/2.0.4/mountnew.sh,https://{wgetpath}/sunbox3/shell/2.0.4/ganglia-agent.sh,https://{wgetpath}/sunbox3/shell/2.0.4/ganglia-server.sh','-e "type=agent host={ambarihost} dburl={ambaridb} dbport={ambaridbport} dbname={ambaridbname} dbuser={dbuser} dbpass={dbpassword} domain={domain} wgetpath={wgetpath} clustername={clustername} logblob={logblob} username={username} clusterid={clusterid} ganglialist={ganglialist} installganglia={installganglia} zone={zone}  miclientid={miclientid}"',0,'niyang','2023-02-19 10:20:37'),
         ('3a09171f5b8942c3b1d97d62aa44e954','a02a9a7a1eed435e8816684be3c8392c','HDFS带宽调整','bandwidth','https://{wgetpath}/sunbox3/shell/2.0.4/bandwidth.yaml','https://{wgetpath}/sunbox3/shell/2.0.4/bandwidth.sh','-e "username={username}  bandwidth={bandwidth}"',0,'niyang','2023-02-19 10:20:37'),
         ('9cfc02faae2b433f8353bba319a7cad5','a02a9a7a1eed435e8816684be3c8392c','磁盘扩容脚本','diskscaleout','https://{wgetpath}/sunbox3/shell/2.0.4/expandisk.yaml ','https://{wgetpath}/sunbox3/shell/2.0.4/expandisk.sh ','-e "username={username}"',0,'niyang','2023-02-19 10:20:37'),
         ('09a81359bfaa43349c699ff10abc8511','a02a9a7a1eed435e8816684be3c8392c','执行HDFS FSCK','run_hdfs_fsck','','https://{wgetpath}/sunbox3/shell/2.0.4/hdfs.sh','',0,'niyang','2023-02-19 10:20:37'),
         ('538e1ba69f5243d69676f6f478cc77dc','a02a9a7a1eed435e8816684be3c8392c','安装AmbariServer','install_ambari_server','https://{wgetpath}/sunbox3/shell/2.0.4/ambari.yaml','https://{wgetpath}/sunbox3/shell/2.0.4/ambari-agent.sh,https://{wgetpath}/sunbox3/shell/2.0.4/ambari-server.sh,https://{wgetpath}/sunbox3/shell/2.0.4/initialize.sh,https://{wgetpath}/sunbox3/shell/2.0.4/mountnew.sh,https://{wgetpath}/sunbox3/shell/2.0.4/ganglia-agent.sh,https://{wgetpath}/sunbox3/shell/2.0.4/ganglia-server.sh','-e "type=server host={ambarihost} dburl={ambaridb} dbport={ambaridbport} dbname={ambaridbname} dbuser={dbuser} dbpass={dbpassword} domain={domain} wgetpath={wgetpath} clustername={clustername} logblob={logblob} username={username} clusterid={clusterid} ganglialist={ganglialist} installganglia={installganglia} zone={zone} miclientid={miclientid} isembedambaridb={isembedambaridb}"',0,'niyang','2023-02-19 10:20:37');

update base_release_vm_img set img_id='a02a9a7a1eed435e8816684be3c8392c',os_imageid ='' where release_version ='SDP-1.0';













