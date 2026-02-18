
update base_release_vm_img set img_id='85611fe214304e9dd15dbd10db6227ea'
where (img_id is null or img_id ='')
and  release_version='SDP-1.0';


replace into base_images (img_id,os_image_id ,os_image_type ,os_version,created_time,createdby)
select img_id,os_imageid ,os_image_type ,os_version ,now() as created_time ,'flyway' as createdby
from base_release_vm_img brvi
where img_id='85611fe214304e9dd15dbd10db6227ea'
limit 1;

delete from base_image_scripts where img_id='85611fe214304e9dd15dbd10db6227ea';

insert into base_image_scripts(img_script_id, img_id, script_name, run_timing, playbook_uri,
                               script_file_uri, extra_vars, sort_no, createdby, created_time)
values ('d9a6564b-7853-11ed-85b7-6045bdc7fdca','85611fe214304e9dd15dbd10db6227ea','安装AmbariServer','install_ambari_server',
        'https://{wgetpath}/sunbox3/shell/ambari.yaml','https://{wgetpath}/sunbox3/shell/ambari-agent.sh,https://{wgetpath}/sunbox3/shell/ambari-server.sh,https://{wgetpath}/sunbox3/shell/initialize.sh,https://{wgetpath}/sunbox3/shell/mountnew.sh',
        '-e "type=server host={ambarihost} dburl={ambaridb} dbport={ambaridbport} dbname={ambaridbname} dbuser={dbuser} dbpass={dbpassword} domain={domain} wgetpath={wgetpath} clustername={clustername} logblob={logblob} username={username}"
',0,'flyway',now()),
       ('f9a6564b-7853-11ed-85b7-6045bdc7fdca','85611fe214304e9dd15dbd10db6227ea','安装AmbariAgent','install_ambari_agent',
        'https://{wgetpath}/sunbox3/shell/ambari.yaml','https://{wgetpath}/sunbox3/shell/ambari-agent.sh,https://{wgetpath}/sunbox3/shell/ambari-server.sh,https://{wgetpath}/sunbox3/shell/initialize.sh,https://{wgetpath}/sunbox3/shell/mountnew.sh',
        '-e "type=agent host={ambarihost} dburl={ambaridb} dbport={ambaridbport} dbname={ambaridbname} dbuser={dbuser} dbpass={dbpassword} domain={domain} wgetpath={wgetpath}  clustername={clustername} logblob={logblob} username={username}"',0,'flyway',now());

update conf_cluster_vm set img_id='85611fe214304e9dd15dbd10db6227ea' where img_id is null or img_id='';
