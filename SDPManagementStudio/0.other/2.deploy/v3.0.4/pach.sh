#!/bin/bash

blobpath=$1

#wget -P /usr/local/ https://sasdpscriptstmp.blob.core.windows.net/sunbox3/shell/query_scheduled_event.py
wget -P /usr/local/ ${blobpath}
chmod 755 /usr/local/query_scheduled_event.py

cat > /etc/fair-scheduler.xml << EOF
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<allocations>
    <queue name="root">
        <weight>1.0</weight>
        <schedulingPolicy>drf</schedulingPolicy>
        <aclSubmitApps> </aclSubmitApps>
        <aclAdministerApps>*</aclAdministerApps>
        <queue name="default">
            <maxRunningApps>100</maxRunningApps>
            <weight>1.0</weight>
            <schedulingPolicy>drf</schedulingPolicy>
            <aclSubmitApps>*</aclSubmitApps>
            <aclAdministerApps>*</aclAdministerApps>
        </queue>
        </queue>
</allocations>
EOF
chmod 755 /etc/fair-scheduler.xml


rm -rf /usr/local/mysql/bin/mysql
cp /usr/bin/mysql /usr/local/mysql/bin/
chmod 755 /usr/local/mysql/bin/mysql


cat > /usr/local/metric.sh << EOF
#!/bin/bash

course=\`ps -ef |grep metric.sh\`
echo "进程信息为：\${course}"

count=\`ps -ef |grep metric.sh|wc -l\`
if [[ "\$count" -gt 4 ]];then
        echo "进程已存在"
        exit
fi

curl -H Metadata:true http://169.254.169.254/metadata/scheduledevents?api-version=2020-07-01
isreboot=\$(curl -H Metadata:true http://169.254.169.254/metadata/scheduledevents?api-version=2020-07-01 2>&1 |grep Reboot|wc -l)
isredeploy=\$(curl -H Metadata:true http://169.254.169.254/metadata/scheduledevents?api-version=2020-07-01 2>&1 |grep Redeploy|wc -l)

echo "\${isreboot}"
echo "\${isredeploy}"
if [[ "\${isreboot}" -gt 0 ]]||[[ "\${isredeploy}" -gt 0 ]]; then
        systemctl stop mysqld.service
        sleep 40m
        systemctl start mysqld.service
fi
EOF

chmod 755 /usr/local/metric.sh