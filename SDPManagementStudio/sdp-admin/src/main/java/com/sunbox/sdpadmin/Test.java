package com.sunbox.sdpadmin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sunbox.sdpadmin.model.shein.request.InstanceGroupAddConfig;
import com.sunbox.sdpadmin.model.shein.request.InstanceGroupNewConfigElement;
import com.sunbox.sdpadmin.model.shein.request.SheinRequestModel;
import com.sunbox.sdpadmin.model.shein.response.SheinResponseModel;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {
    public static void main(String[] args) {
        String sd="^[A-Za-z0-9]{1}[-A-Za-z0-9]{0,38}[A-Za-z0-9]{1}$";
        Pattern pattern= Pattern.compile(sd);
        Matcher m=pattern.matcher("-sdp-SeJy5J9kD4n11111111111111111");
        System.out.println(m.matches());

        JSONArray jsonArray=JSON.parseArray("[\n" +
                "  {\n" +
                "    \"dictId\": null,\n" +
                "    \"pdictId\": null,\n" +
                "    \"dictName\": \"sdp-sit-ssh-key-pair\",\n" +
                "    \"dictValue\": \"sdp-sit-ssh-public-key\",\n" +
                "    \"aliasName\": null,\n" +
                "    \"isDelete\": null,\n" +
                "    \"sortno\": null,\n" +
                "    \"createdby\": null,\n" +
                "    \"createdTime\": null,\n" +
                "    \"modifiedby\": null,\n" +
                "    \"modifiedTime\": null\n" +
                "  }\n" +
                "]");


        /*String json = "{\"emrManagedScalingPolicy\":{\"maxCoreNodeUnit\":800,\"maxUnit\":5000,\"onDemandUnitLimit\":800,\"minUnit\":64},\"instanceKeyPair\":\"abc-dw-emr-uswest2\",\"subnet\":\"subnet-01d16f1dafa1aabbd\",\"computerTags\":[],\"clusterCfgs\":[{\"cfg\":{\"dfs.replication\":\"2\",\"dfs.permissions\":\"false\"},\"classification\":\"hdfs-site\"},{\"cfg\":{\"yarn.scheduler.capacity.root.acl_submit_applications\":\"*\",\"yarn.scheduler.capacity.root.default.acl_submit_applications\":\"hadoop\",\"yarn.scheduler.capacity.root.default.acl_administer_queue\":\"hadoop\",\"yarn.scheduler.capacity.root.acl_administer_queue\":\"*\"},\"classification\":\"capacity-scheduler\"},{\"cfg\":{\"YARN_TIMELINESERVER_HEAPSIZE\":\"4096\"},\"classification\":\"yarn-env\"},{\"cfg\":{\"yarn.timeline-service.write-direct-logs-to-HDFS\":\"false\",\"yarn.resourcemanager.system-metrics-publisher.enabled\":\"true\",\"yarn.log-aggregation.retain-seconds\":\"7200\",\"yarn.resourcemanager.system-metrics-publisher.timeline-server-v1.enable-batch\":\"true\",\"yarn.scheduler.minimum-allocation-mb\":\"2048\",\"yarn.timeline-service.ttl-ms\":\"172800000\"},\"classification\":\"yarn-site\"},{\"cfg\":{\"hive.server2.authentication.ldap.baseDN\":\"dc=corp,dc=emr,dc=com\",\"mapreduce.map.speculative\":\"false\",\"mapred.map.tasks.speculative.execution\":\"false\",\"hive.mapred.mode\":\"nostrict\",\"javax.jdo.option.ConnectionDriverName\":\"org.mariadb.jdbc.Driver\",\"javax.jdo.option.ConnectionURL\":\"jdbc:mysql://abc-dw-hive-meta-prod-us.c6mtqyik6gyo.us-west-2.rds.amazonaws.com:3306/metastore?createDatabaseIfNotExist=True\",\"hive.stats.fetch.column.stats\":\"false\",\"hive.merge.size.per.task\":\"256000000\",\"hive.async.log.enabled\":\"false\",\"hive.blobstore.optimizations.enabled\":\"false\",\"hive.merge.smallfiles.avgsize\":\"256000000\",\"hive.server2.authentication\":\"LDAP\",\"hive.merge.mapredfiles\":\"true\",\"hive.merge.tezfiles\":\"true\",\"hive.exec.orc.split.strategy\":\"ETL\",\"hive.server2.authentication.ldap.userDNPattern\":\"CN=%s,CN=users,DC=corp,DC=emr,DC=com\",\"hive.security.authorization.sqlstd.confwhitelist.append\":\".*\\\\\\\\..*\\\\\\\\..*\",\"hive.metastore.warehouse.dir\":\"s3://abc-dw-prod-us/libs/hive/warehouse/\",\"hive.server2.async.exec.threads\":\"700\",\"hive.auto.convert.join.noconditionaltask.size\":\"10000000\",\"hive.server2.authentication.ldap.url\":\"ldaps://dwldap-us.sheinbackend.com\",\"hive.execution.engine\":\"tez\",\"mapred.reduce.tasks.speculative.execution\":\"false\",\"hive.strict.checks.cartesian.product\":\"false\",\"javax.jdo.option.ConnectionUserName\":\"dsbi\",\"hive.auto.convert.join\":\"false\",\"javax.jdo.option.ConnectionPassword\":\"dsbiforaws\",\"hive.merge.mapfiles\":\"true\",\"hive.mapred.supports.subdirectories\":\"true\",\"distcp.options.m\":\"300\",\"hive.server2.in.place.progress\":\"false\",\"hive.mapred.reduce.tasks.speculative.execution\":\"faldse\",\"mapreduce.input.fileinputformat.input.dir.recursive\":\"true\",\"hive.server2.builtin.udf.blacklist\":\"empty_blacklist\",\"mapreduce.reduce.speculative\":\"false\",\"tez.grouping.min-size\":\"256000000\"},\"classification\":\"hive-site\"},{\"cfg\":{\"fs.s3.consistent.retryPeriodSeconds\":\"10\",\"fs.s3.consistent\":\"false\",\"fs.s3.retryPeriodSeconds\":\"15\",\"fs.s3.consistent.retryPolicyType\":\"fixed\",\"fs.s3.consistent.retryCount\":\"9\",\"fs.s3.maxRetries\":\"160\",\"fs.s3.consistent.metadata.tableName\":\"EmrFSMetadata\"},\"classification\":\"emrfs-site\"},{\"cfg\":{\"tez.am.node-unhealthy-reschedule-tasks\":\"true\"},\"classification\":\"tez-site\"},{\"cfg\":{\"HADOOP_HEAPSIZE\":\"40960\"},\"classification\":\"hive-env\"},{\"cfg\":{\"mapred.output.committer.class\":\"org.apache.hadoop.mapred.FileOutputCommitter\",\"mapreduce.fileoutputcommitter.algorithm.version\":\"2\"},\"classification\":\"mapred-site\"}],\"serviceRole\":\"EMR_DefaultRole\",\"instanceFleetNewConfigs\":[{\"targetSpotCapacity\":0,\"candidateInsConfigs\":[{\"volumeType\":\"gp2\",\"volumeSizeInGB\":300,\"insType\":\"m5.4xlarge\",\"insEquivalentUnit\":1}],\"targetOnDemandCapacity\":1,\"insFtRole\":\"MASTER\"},{\"targetSpotCapacity\":0,\"candidateInsConfigs\":[{\"volumeType\":\"gp2\",\"volumeSizeInGB\":2000,\"insType\":\"m5.4xlarge\",\"insEquivalentUnit\":16}],\"targetOnDemandCapacity\":800,\"insFtRole\":\"CORE\"},{\"targetSpotCapacity\":5000,\"candidateInsConfigs\":[{\"volumeType\":\"gp2\",\"volumeSizeInGB\":500,\"insType\":\"m5d.12xlarge\",\"insEquivalentUnit\":48},{\"volumeType\":\"gp2\",\"volumeSizeInGB\":500,\"insType\":\"m5d.8xlarge\",\"insEquivalentUnit\":32},{\"volumeType\":\"gp2\",\"volumeSizeInGB\":500,\"insType\":\"m4.16xlarge\",\"insEquivalentUnit\":64},{\"volumeType\":\"gp2\",\"volumeSizeInGB\":500,\"insType\":\"m4.10xlarge\",\"insEquivalentUnit\":40},{\"volumeType\":\"gp2\",\"volumeSizeInGB\":500,\"insType\":\"m5.12xlarge\",\"insEquivalentUnit\":48}],\"targetOnDemandCapacity\":0,\"insFtRole\":\"TASK\"}],\"stepCompleteClose\":false,\"rootVolSize\":100,\"cmdbServerName\":\"dataQueryPlatform-uswest2-prod-eksUsAbcProdMaster2000\",\"startHa\":0,\"clusterName\":\"abc-prod-dataplatform-schdtrack-ranger\",\"terminationProtected\":false,\"tagMap\":{\"svcid\":\"26735072-1c67-4729-8077-2a854b3c80c0\",\"svc\":\"dataQueryPlatform-uswest2-prod-eksUsAbcProdMaster2000\",\"childService\":\"SchdTrack\",\"service\":\"dw\",\"for\":\"盛昕鑫\",\"remark\":\"调度埋点集群\",\"env\":\"lt-prod\",\"dep\":\"abc\"},\"group\":\"dw\",\"clusterApps\":[\"Hadoop\",\"Hive\",\"Sqoop\",\"Tez\",\"Ganglia\",\"HCatalog\",\"Pig\"],\"serviceAccessSecurityGroup\":\"sg-077425fdd78819119\",\"clusterReleaseVer\":\"emr-5.32.1\",\"instanceCollectionType\":1,\"slaveSecurityGroup\":\"sg-0e5361ab5cbf7e986\",\"instanceRole\":\"EMR_EC2_DefaultRole\",\"cmdbUid\":\"26735072-1c67-4729-8077-2a854b3c80c0\",\"clusterJarSteps\":[{\"actionOnFailure\":\"CONTINUE\",\"jarStep\":{\"args\":[],\"jarName\":\"s3://abc-dw-prod-us/config/replace_sqoop_jar.jar\"}},{\"actionOnFailure\":\"CONTINUE\",\"jarStep\":{\"args\":[],\"jarName\":\"s3://abc-dw-prod-us/config/config_atlas_hook_530.jar\"}},{\"actionOnFailure\":\"CONTINUE\",\"jarStep\":{\"args\":[\"s3://abc-dw-prod-us/config/dataabc/commandserver/install-command-server.sh\"],\"jarName\":\"s3://abc-dw-prod-us/config/script-runner.jar\"}}],\"s3LogLocation\":\"s3://abc-awssvc-us/emr/logs/abc-prod-dataplatform-schdtrack-ranger/\",\"autoCloseStrategy\":\"no\",\"createUser\":\"一站式数据平台\",\"isIdle\":false,\"cfgBkt\":\"\",\"autoScalingRole\":\"EMR_AutoScaling_DefaultRole\",\"masterSecurityGroup\":\"sg-084eda33293732d71\",\"internalIpOnly\":true,\"bootstrapActions\":{\"s3://abc-dw-prod-us/config/patch-log4j-emr-5.32.1-v1.sh\":\"\",\"s3://abc-dw-prod-us/config/set_timezone_new.sh\":\"\",\"s3://abc-dw-prod-us/config/set_userdefined.sh\":\"\",\"s3://abc-dw-prod-us/config/config_geoip.sh\":\"\",\"s3://abc-dw-prod-us/config/config_data_encrypt.sh\":\"\",\"s3://abc-dw-prod-us/config/set_software_upgrade_linux.sh\":\"\",\"s3://abc-dw-prod-us/datashield/scripts/emr-install-ranger-prd-us.sh\":\"--hive=True\"},\"dc\":\"us\"}";
        SheinRequestModel sheinRequestModel = JSON.parseObject(json,SheinRequestModel.class);
//        SheinRequestModel sheinRequestModel = new SheinRequestModel();

        String uuid = UUID.randomUUID().toString();
        Map map = new HashMap();
        map.put("cluster_id",uuid);
        SheinResponseModel sheinResponseModel = new SheinResponseModel();
        sheinResponseModel.setCode("200");
        sheinResponseModel.setMsg("成功");
        sheinResponseModel.setInfo(map);
//        System.out.println(JSON.toJSONString(sheinResponseModel));

        InstanceGroupNewConfigElement instanceGroupNewConfigElement = new InstanceGroupNewConfigElement();
        InstanceGroupAddConfig instanceGroupAddConfig = new InstanceGroupAddConfig();
        instanceGroupAddConfig.setInsGpCnt(10);
        instanceGroupAddConfig.setVolumeSizeInGB("1000");
        instanceGroupAddConfig.setInsType("EMR");
        instanceGroupAddConfig.setInsGpRole("Master");
        instanceGroupAddConfig.setInsMktType("ondemond");
        instanceGroupAddConfig.setVolumeType("SSD");
        instanceGroupNewConfigElement.setInstanceGroupAddConfig(instanceGroupAddConfig);
        List<InstanceGroupNewConfigElement> instanceGroupNewConfigElementList = new ArrayList<>();
        instanceGroupNewConfigElementList.add(instanceGroupNewConfigElement);
        sheinRequestModel.setInstanceGroupNewConfigs(instanceGroupNewConfigElementList);
        System.out.println(JSON.toJSONString(sheinRequestModel));*/
    }
}
