-- 更新Ganglia默认配置
UPDATE ambari_config_item
	SET value='
*.period=300

*.sink.ganglia.class=org.apache.hadoop.metrics2.sink.ganglia.GangliaSink31
# *.sink.ganglia.period=10

# default for supportsparse is false
*.sink.ganglia.supportsparse=true

.sink.ganglia.slope=jvm.metrics.gcCount=zero,jvm.metrics.memHeapUsedM=both
.sink.ganglia.dmax=jvm.metrics.threadsBlocked=70,jvm.metrics.memHeapUsedM=40

# Hook up to the server
namenode.sink.ganglia.servers=239.2.11.71
datanode.sink.ganglia.servers=239.2.11.71
jobtracker.sink.ganglia.servers=239.2.11.71
tasktracker.sink.ganglia.servers=239.2.11.71
maptask.sink.ganglia.servers=239.2.11.71
reducetask.sink.ganglia.servers=239.2.11.71
resourcemanager.sink.ganglia.servers=239.2.11.71
nodemanager.sink.ganglia.servers=239.2.11.71
historyserver.sink.ganglia.servers=239.2.11.71
journalnode.sink.ganglia.servers=239.2.11.71
nimbus.sink.ganglia.servers=239.2.11.71
supervisor.sink.ganglia.servers=239.2.11.71

resourcemanager.sink.ganglia.tagsForPrefix.yarn=Queue


{% if has_metric_collector %}

*.period={{metrics_collection_period}}
*.sink.timeline.plugin.urls=file:///usr/lib/ambari-metrics-hadoop-sink/ambari-metrics-hadoop-sink.jar
*.sink.timeline.class=org.apache.hadoop.metrics2.sink.timeline.HadoopTimelineMetricsSink
*.sink.timeline.period={{metrics_collection_period}}
*.sink.timeline.sendInterval={{metrics_report_interval}}000
*.sink.timeline.slave.host.name={{hostname}}
*.sink.timeline.zookeeper.quorum={{zookeeper_quorum}}
*.sink.timeline.protocol={{metric_collector_protocol}}
*.sink.timeline.port={{metric_collector_port}}
*.sink.timeline.instanceId = {{cluster_name}}
*.sink.timeline.set.instanceId = {{set_instanceId}}
*.sink.timeline.host_in_memory_aggregation = {{host_in_memory_aggregation}}
*.sink.timeline.host_in_memory_aggregation_port = {{host_in_memory_aggregation_port}}
{% if is_aggregation_https_enabled %}
*.sink.timeline.host_in_memory_aggregation_protocol = {{host_in_memory_aggregation_protocol}}
{% endif %}

# HTTPS properties
*.sink.timeline.truststore.path = {{metric_truststore_path}}
*.sink.timeline.truststore.type = {{metric_truststore_type}}
*.sink.timeline.truststore.password = {{metric_truststore_password}}

datanode.sink.timeline.collector.hosts={{ams_collector_hosts}}
namenode.sink.timeline.collector.hosts={{ams_collector_hosts}}
resourcemanager.sink.timeline.collector.hosts={{ams_collector_hosts}}
nodemanager.sink.timeline.collector.hosts={{ams_collector_hosts}}
jobhistoryserver.sink.timeline.collector.hosts={{ams_collector_hosts}}
journalnode.sink.timeline.collector.hosts={{ams_collector_hosts}}
maptask.sink.timeline.collector.hosts={{ams_collector_hosts}}
reducetask.sink.timeline.collector.hosts={{ams_collector_hosts}}
applicationhistoryserver.sink.timeline.collector.hosts={{ams_collector_hosts}}

resourcemanager.sink.timeline.tagsForPrefix.yarn=Queue

{% if is_nn_client_port_configured %}
# Namenode rpc ports customization
namenode.sink.timeline.metric.rpc.client.port={{nn_rpc_client_port}}
{% endif %}
{% if is_nn_dn_port_configured %}
namenode.sink.timeline.metric.rpc.datanode.port={{nn_rpc_dn_port}}
{% endif %}
{% if is_nn_healthcheck_port_configured %}
namenode.sink.timeline.metric.rpc.healthcheck.port={{nn_rpc_healthcheck_port}}
{% endif %}

{% endif %}',
updated_by = 'system',
updated_time = now()
	WHERE service_code = 'HDFS' and config_type_code = 'hadoop-metrics2.properties' and state = 'VALID';
