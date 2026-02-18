UPDATE ambari_config_item
	SET value='*.period=300
*.sink.ganglia.class=org.apache.hadoop.metrics2.sink.ganglia.GangliaSink31
#
*.sink.ganglia.period=10
# default for supportsparse is false
*.sink.ganglia.supportsparse=true
*.sink.ganglia.slope=jvm.metrics.gcCount=zero,jvm.metrics.memHeapUsedM=both
*.sink.ganglia.dmax=jvm.metrics.threadsBlocked=70,jvm.metrics.memHeapUsedM=40
# Hook up to the server
namenode.sink.ganglia.servers=239.2.11.71
datanode.sink.ganglia.servers=239.2.11.71
#jobtracker.sink.ganglia.servers=239.2.11.71
#tasktracker.sink.ganglia.servers=239.2.11.71
#maptask.sink.ganglia.servers=239.2.11.71
#reducetask.sink.ganglia.servers=239.2.11.71
resourcemanager.sink.ganglia.servers=239.2.11.71
nodemanager.sink.ganglia.servers=239.2.11.71
#historyserver.sink.ganglia.servers=239.2.11.71
#journalnode.sink.ganglia.servers=239.2.11.71
#nimbus.sink.ganglia.servers=239.2.11.71
#supervisor.sink.ganglia.servers=239.2.11.71
resourcemanager.sink.ganglia.tagsForPrefix.yarn=Queue'
	WHERE config_type_code = 'hadoop-metrics2.properties' and `key` = 'content';
