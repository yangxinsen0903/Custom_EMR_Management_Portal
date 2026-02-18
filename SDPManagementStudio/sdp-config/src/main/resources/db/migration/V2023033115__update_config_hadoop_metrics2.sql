delete from ambari_config_item where config_type_code='hadoop-metrics2.properties' and `key` = 'content';

INSERT INTO ambari_config_item (stack_code,service_code,component_code,config_type_code,`key`,value,is_content_prop,is_dynamic,dynamic_type,item_type,state,created_by,created_time,updated_by,updated_time) VALUES
                                                                                                                                                                                                                 ('SDP-1.0','HDFS','NULL','hadoop-metrics2.properties','content','*.period=300
*.sink.ganglia.class=org.apache.hadoop.metrics2.sink.ganglia.GangliaSink31
#
*.sink.ganglia.period=10
# default for supportsparse is false
*.sink.ganglia.supportsparse=true
*.sink.ganglia.slope=jvm.metrics.gcCount=zero,jvm.metrics.memHeapUsedM=both
*.sink.ganglia.dmax=jvm.metrics.threadsBlocked=70,jvm.metrics.memHeapUsedM=40
# Hook up to the server
namenode.sink.ganglia.servers=sdp-hiveleo329-1-100-amb-0001.sit.sdp.com
datanode.sink.ganglia.servers=sdp-hiveleo329-1-100-amb-0001.sit.sdp.com
resourcemanager.sink.ganglia.servers=sdp-hiveleo329-1-100-amb-0001.sit.sdp.com
nodemanager.sink.ganglia.servers=sdp-hiveleo329-1-100-amb-0001.sit.sdp.com

*.source.filter.class=org.apache.hadoop.metrics2.filter.GlobFilter
nodemanager.*.source.filter.exclude=*ContainerResource*',1,0,NULL,'NON_HA','VALID','system',now(),'system',now()),
                                                                                                                                                                                                                 ('SDP-1.0','HDFS','NULL','hadoop-metrics2.properties','content','*.period=300
*.sink.ganglia.class=org.apache.hadoop.metrics2.sink.ganglia.GangliaSink31
#
*.sink.ganglia.period=10
# default for supportsparse is false
*.sink.ganglia.supportsparse=true
*.sink.ganglia.slope=jvm.metrics.gcCount=zero,jvm.metrics.memHeapUsedM=both
*.sink.ganglia.dmax=jvm.metrics.threadsBlocked=70,jvm.metrics.memHeapUsedM=40
# Hook up to the server
namenode.sink.ganglia.servers=sdp-hiveleo329-1-100-amb-0001.sit.sdp.com
datanode.sink.ganglia.servers=sdp-hiveleo329-1-100-amb-0001.sit.sdp.com
resourcemanager.sink.ganglia.servers=sdp-hiveleo329-1-100-amb-0001.sit.sdp.com
nodemanager.sink.ganglia.servers=sdp-hiveleo329-1-100-amb-0001.sit.sdp.com

*.source.filter.class=org.apache.hadoop.metrics2.filter.GlobFilter
nodemanager.*.source.filter.exclude=*ContainerResource*',1,0,NULL,'HA','VALID','system',now(),'system',now());
