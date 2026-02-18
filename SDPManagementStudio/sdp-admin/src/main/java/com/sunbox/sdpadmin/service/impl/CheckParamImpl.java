package com.sunbox.sdpadmin.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sunbox.domain.*;
import com.sunbox.domain.metaData.*;
import com.sunbox.dao.mapper.BaseReleaseVersionMapper;
import com.sunbox.sdpadmin.mapper.BaseSceneAppsMapper;
import com.sunbox.sdpadmin.mapper.BaseSceneMapper;
import com.sunbox.sdpadmin.model.admin.request.AdminSaveClusterRequest;
import com.sunbox.sdpadmin.model.shein.request.DbCfgs;
import com.sunbox.sdpadmin.model.shein.request.InstanceGroupAddConfig;
import com.sunbox.sdpadmin.model.shein.request.InstanceGroupNewConfigElement;
import com.sunbox.sdpadmin.model.shein.request.SheinRequestModel;
import com.sunbox.sdpadmin.service.AdminApiService;
import com.sunbox.sdpadmin.service.ICheckParam;
import com.sunbox.sdpadmin.service.IComposeMetaService;
import com.sunbox.service.IMetaDataItemService;
import com.sunbox.util.httpClient;
import com.sunbox.web.BaseCommonInterFace;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author : [niyang]
 * @className : CheckParamImpl
 * @description : [描述说明该类的功能]
 * @createTime : [2023/1/4 10:58 AM]
 */
@Service
public class CheckParamImpl implements ICheckParam, BaseCommonInterFace {


    @Value("${sdp.tag.pattern:^[\\u4E00-\\u9FA5-A-Za-z0-9]{1,100}$}")
    private String tagkeyPattern;

    @Value("${sdp.clustername.pattern:^[A-Za-z0-9]{1}[-A-Za-z0-9]{0,38}[A-Za-z0-9]{1}$}")
    private String clusterPattern;

    @Value("${sdp.datadisksize.max:16000}")
    private String datadiskmaxsize;

    @Value("${sdp.datadisksize.min:200}")
    private String datadiskminsize;

    @Value("${sdp.rootdisksize.max:2000}")
    private String rootdiskmaxsize;

    @Value("${sdp.rootdisksize.min:100}")
    private String rootdiskminsize;

    @Value("${hive.db.check:false}")
    private String hivedbcheck;

    /**
     * code节点最小节点数
     */
    @Value("${sdp.node.core.count.min:2}")
    private String coreNodeMinCount;

    /**
     * code节点最大节点数
     */
    @Value("${sdp.node.core.count.max:99}")
    private String coreNodeMaxCount;

    /**
     * task节点最小节点数
     */
    @Value("${sdp.node.task.count.min:0}")
    private String taskNodeMinCount;


    @Value("${spring.datasource.driver-class-name}")
    private String mysqldriverclass;


    /**
     * task节点最大节点数
     */
    @Value("${sdp.node.task.count.max:1999}")
    private String taskNodeMaxCount;

    @Autowired
    private BaseReleaseVersionMapper baseReleaseVersionMapper;

    @Autowired
    private BaseSceneMapper baseSceneMapper;

    @Autowired
    private IComposeMetaService composeMetaService;

    @Autowired
    private BaseSceneAppsMapper baseSceneAppsMapper;

    @Autowired
    private AdminApiService adminApiService;

    @Autowired
    private IMetaDataItemService metaDataItemService;
    @Autowired
    private SheinApiServiceImpl sheinApiServiceImpl;

    /**
     * 校验时间参数格式是否正确
     *
     * @param datetimeStr 时间字符串
     * @param format      时间格式化字符串
     * @return
     */
    @Override
    public ResultMsg checkDateTimeFormat(String datetimeStr, String format) {
        ResultMsg msg=new ResultMsg();
        try {
            if (StringUtils.isEmpty(datetimeStr)){
                msg.setResult(true);
                return msg;
            }
            LocalDateTime.parse(datetimeStr, DateTimeFormatter.ofPattern(format));
            msg.setResult(true);
        }catch (Exception e){
            msg.setResult(false);
            msg.setErrorMsg("时间参数格式异常，请使用"+format);
        }
        return msg;
    }


    /**
     * 校验起止时间的有效性
     *
     * @param begTime 开始时间
     * @param endTime 结束时间
     * @param format
     * @return
     */
    @Override
    public ResultMsg checkBEDateTimeValid(String begTime, String endTime,String format) {
        ResultMsg msg=new ResultMsg();
        try {
            if (StringUtils.isEmpty(begTime)){
                msg.setResult(true);
                return msg;
            }

            if (StringUtils.isEmpty(endTime)){
                msg.setResult(true);
                return msg;
            }

            try {
                LocalDateTime begdate= LocalDateTime.parse(begTime,DateTimeFormatter.ofPattern(format));
                LocalDateTime enddate=LocalDateTime.parse(endTime,DateTimeFormatter.ofPattern(format));
                if (begdate.isAfter(enddate)){
                    msg.setResult(false);
                    msg.setErrorMsg("无效的时间参数。");
                    return msg;
                }
                msg.setResult(true);
            }catch (Exception e){
                msg.setResult(false);
                msg.setErrorMsg("无效的时间参数。");
                getLogger().error("时间校验数据异常，",e);
                return msg;
            }
        }catch (Exception e){
            msg.setResult(false);
            msg.setErrorMsg("无效的时间参数。");
            getLogger().error("时间校验数据异常，",e);
            return msg;
        }
        return msg;
    }

    /**
     * shein接口Create接口参数检查
     *
     * @param requestModel
     * @return
     */
    @Override
    public ResultMsg checkSheinCreateParam(SheinRequestModel requestModel) {
        ResultMsg msg = new ResultMsg();

        getLogger().info("checkdata:" + JSON.toJSONString(requestModel));
        // 1.common
        ResultMsg msg1 = checkCreateCommonString("dc,az,masterSecurityGroup,slaveSecurityGroup," +
                        "vNet,subnet,instanceKeyPair,vmMI,rootVolType,s3LogLocation,logMI,scene,clusterReleaseVer",
                requestModel);
        if (!msg1.getResult()) {
            return msg1;
        }

        // 删除保护
        ResultMsg msg16 = checkdeleteProtected(requestModel);
        if (!msg16.getResult()) {
            return msg16;
        }

        ResultMsg msg17 = checkRootDiskSize(requestModel.getRootVolSize());
        if (!msg17.getResult()) {
            return msg17;
        }

        ResultMsg msg18 = checkStartHa(requestModel);
        if (!msg18.getResult()) {
            return msg18;
        }

        // 2. 集群名称校验
        ResultMsg msg2 = checkClusterName(requestModel.getClusterName());
        if (!msg2.getResult()) {
            return msg2;
        }

        // 3.checkClusterReleaseVer
        ResultMsg msg3 = checkClusterReleaseVer(requestModel.getClusterReleaseVer());
        if (!msg3.getResult()) {
            return msg3;
        }

        // 4.checkAmbariDb
        if (requestModel.getIsEmbedAmbariDb() == null || requestModel.getIsEmbedAmbariDb().equals(0)) {
            // 非内置数据库生效
            ResultMsg msg4 = checkAmbariDb(requestModel.getAmbariDbCfgs(), requestModel.getClusterName());
            if (!msg4.getResult()) {
                return msg4;
            }
        }

        // 5.checkhivemetadb
        ResultMsg msg5 = checkHiveMetaDb(requestModel.getHiveMetadataDbCfgs(), requestModel.getClusterApps());
        if (!msg5.getResult()) {
            return msg5;
        }

        // checksecene
        ResultMsg msg6 = checkScene(requestModel.getScene(), requestModel.getClusterReleaseVer());
        if (!msg6.getResult()) {
            return msg6;
        }

        //
        ResultMsg msg7 = checkInstanceGroupNewConfigs(requestModel.getInstanceGroupNewConfigs(), requestModel);
        if (!msg7.getResult()) {
            return msg7;
        }

        // 自定义脚本
        ResultMsg msg8 = checkConfClusterScript(requestModel);
        if (!msg8.getResult()) {
            return msg8;
        }

        // 密钥对
        ResultMsg msg9 = checkKayPair(requestModel);
        if (!msg9.getResult()) {
            return msg9;
        }

        // 可用区
        ResultMsg msg10 = checkAz(requestModel);
        if (!msg10.getResult()) {
            return msg10;
        }

        // checknetwork
        ResultMsg msg11 = checkNetWork(requestModel);
        if (!msg11.getResult()) {
            return msg11;
        }

        // 安全组
        ResultMsg msg12 = checkNsg(requestModel);
        if (!msg12.getResult()) {
            return msg12;
        }

        // 子网
        ResultMsg msg13 = checksubnet(requestModel);
        if (!msg13.getResult()) {
            return msg13;
        }

        // MI
        ResultMsg msg14 = checkMI(requestModel);
        if (!msg14.getResult()) {
            return msg14;
        }

        // logpath
        ResultMsg msg15 = checkLogPath(requestModel);
        if (!msg15.getResult()) {
            return msg15;
        }

        // clusterApps
        ResultMsg msg19 = checkClusterApps(requestModel);
        if (!msg19.getResult()) {
            return msg19;
        }

        // tagMap
        ResultMsg msg20 = checkTagMap(requestModel);
        if (!msg20.getResult()) {
            return msg20;
        }

        ResultMsg msg21 = checkGangliaAssociatedConf(requestModel);
        if (!msg21.getResult()) {
            return msg21;
        }

        msg.setResult(true);
        return msg;
    }

    /**
     * adminApi接口Create接口参数检查
     *
     * @param adminRequestModel
     * @return
     */
    @Override
    public ResultMsg checkAdminApiCreateParam(AdminSaveClusterRequest adminRequestModel) {
        ResultMsg msg=new ResultMsg();
        msg.setResult(true);

        ResultMsg msg1=checkAdminCreateDiskSize(adminRequestModel);
        if (!msg1.getResult()){
            return msg1;
        }
        return msg;
    }


    private ResultMsg checkAdminCreateDiskSize(AdminSaveClusterRequest adminRequestModel){

        AtomicReference<ResultMsg> msg= new AtomicReference<>(new ResultMsg());
        adminRequestModel.getInstanceGroupSkuCfgs().stream().forEach(x->{
            ResultMsg msg1=checkDataDiskSize(x.getDataVolumeSize());
            if (!msg1.getResult()){
                msg.set(msg1);
                return ;
            }
        });
        return msg.get();
    }

    /**
     * 检查ambaridb数据库
     *
     * @param host     数据库主机
     * @param port     端口
     * @param database 数据库名称
     * @param user     用户名
     * @param password 密码
     * @param autocreate 是否自动创建数据库
     * @return
     */
    @Override
    public ResultMsg checkAmbariDb(String host, Integer port, String database, String user,
                                   String password,boolean autocreate) {
        ResultMsg msg=new ResultMsg();
        Connection connect = null;
        try{
            if (StringUtils.isEmpty(host)
                    ||StringUtils.isEmpty(user)
                    ||StringUtils.isEmpty(password)){
                msg.setResult(false);
                msg.setErrorMsg("Ambari数据库参数缺失，必须的参数 user,password, host");
                return msg;
            }

            if (port==null){
                port=3306;
            }
            if(port.compareTo(0)<0){
                msg.setResult(false);
                msg.setErrorMsg("Port 需要是正整数。");
                return msg;
            }

            Class.forName(mysqldriverclass);
            String jdbcurl="jdbc:mysql://"+host +":"+port+"/information_schema?useSSL=false";

            getLogger().info("checkUrl:"+jdbcurl);

            Properties usrinfo=new Properties();
            usrinfo.put("user",user);
            usrinfo.put("password",password);
            connect = DriverManager.getConnection(jdbcurl,usrinfo);
            getLogger().info("get ambaridb connect success.");

            //region 检查是否存在一个空的数据库 tables size=0
            Statement stmt = connect.createStatement();

            String createSql="create database if not exists "+database;

            //region 判断数据库是否存在
            String dbsql=" select * from information_schema.SCHEMATA sc " +
                    "where sc.SCHEMA_NAME ='"+database+"';";
            getLogger().info("ambari_db_check,sql:"+dbsql);

            ResultSet tbrs=stmt.executeQuery(dbsql);
            List tblist=convertList(tbrs);

            if (tblist == null || tblist.size() == 0){
                getLogger().error("Ambari数据库不存在:"+database);
                if (!autocreate){
                    msg.setResult(true);
                    return msg;
                }
                //region 创建数据库
                getLogger().info("createSql:"+createSql);
                // 返回结果不能做为成功与否的依据，
                // 返回ResultSet时为ture
                // false if it is an update count or there are no  results
                boolean res = stmt.execute(createSql);
                //需要重新查询一次确认结果
                ResultSet cktbrs=stmt.executeQuery(dbsql);
                List ckblist=convertList(cktbrs);
                if (ckblist != null && ckblist.size() >0){
                    msg.setResult(true);
                }else{
                    msg.setResult(false);
                    getLogger().error("自动创建数据库失败："+database);
                    msg.setErrorMsg("自动创建数据库失败："+database);
                }
                return msg;
                //endregion
            }
            //endregion

            //region 数据库存在，判断数据表是否存在
            String tbsql=" select * from information_schema.TABLES tb " +
                    "where tb.TABLE_SCHEMA='"+database+"';";
            getLogger().info("ambari_tables_check,sql:"+tbsql);

            ResultSet tbrs2=stmt.executeQuery(tbsql);
            List tblist2=convertList(tbrs2);

            if (tblist2 == null || tblist2.size() == 0){
                getLogger().error("Ambari数据表不存在:"+database);
                if (!autocreate){
                    msg.setResult(true);
                    return msg;
                }

                //region 创建数据库
                getLogger().info("createSql:"+createSql);
                // 返回结果不能做为成功与否的依据，
                // 返回ResultSet时为ture
                // false if it is an update count or there are no  results
                boolean res = stmt.execute(createSql);
                //需要重新查询一次确认结果
                ResultSet cktbrs=stmt.executeQuery(dbsql);
                List ckblist=convertList(cktbrs);

                if (ckblist != null && ckblist.size() >0){
                    msg.setResult(true);
                }else{
                    msg.setResult(false);
                    getLogger().error("自动创建数据库失败："+database);
                    msg.setErrorMsg("自动创建数据库失败："+database);
                }
                return msg;
                //endregion
            }else{
                msg.setResult(false);
                msg.setErrorMsg("ambari数据库不为空。");
                return msg;
            }

            //endregion


        }catch (Exception e) {
            getLogger().info("数据库测试异常"+e.getMessage(),e);
            msg.setResult(false);
            msg.setErrorMsg("Ambari数据库连接异常，请核实数据库参数:"+ ExceptionUtils.getStackTrace(e));
            return msg;
        }finally {
            try {
                if (connect!=null && !connect.isClosed()){
                    connect.close();
                }
            } catch (SQLException e) {
                getLogger().error("JDBC Connection Close exception,",e);
            }
        }
    }

    /**
     * 检查hivemetadb数据库
     *
     * @param host       数据库主机
     * @param port       端口
     * @param database   数据库名称
     * @param user       用户名
     * @param password   密码
     * @return
     */
    @Override
    public ResultMsg checkHiveMetaDb(String host, Integer port, String database, String user, String password) {
        ResultMsg msg=new ResultMsg();
        Connection connect = null;
        try{
            if (StringUtils.isEmpty(host)
                    ||StringUtils.isEmpty(user)
                    ||StringUtils.isEmpty(password)
                    ||StringUtils.isEmpty(database)){
                msg.setResult(false);
                msg.setErrorMsg("HiveMetaData数据库参数缺失，必须的参数 user,password,host,database");
                return msg;
            }

            if (port==null){
               port=3306;
            }

            if(port.compareTo(0)<0){
                msg.setResult(false);
                msg.setErrorMsg("Port 需要是正整数。");
                return msg;
            }

            Class.forName(mysqldriverclass);
            String jdbcurl="jdbc:mysql://"+host+":"+port+"/"+database
                    + "?useSSL=false";
            getLogger().info("HivecheckUrl:"+jdbcurl);

            Properties usrinfo=new Properties();
            usrinfo.put("user",user);
            usrinfo.put("password",password);
            connect = DriverManager.getConnection(jdbcurl, usrinfo);
            if (connect==null || connect.isClosed()){
                msg.setResult(false);
                msg.setErrorMsg("HiveMeta数据库连接异常，请核实数据库参数。");
                return msg;
            }
            //region 检查是否存在数据库
            // 1. 数据库是否存在
            String dbsql="select * from information_schema.SCHEMATA s " +
                    "where s.SCHEMA_NAME ='"+database+"';";
            Statement stmt = connect.createStatement();
            ResultSet rs = stmt.executeQuery(dbsql);
            List rslist=convertList(rs);
            if (rslist==null || rslist.size()==0){
                msg.setResult(false);
                msg.setErrorMsg("hive数据库不存在。");
                return msg;
            }else{
                msg.setResult(true);
                return msg;
            }
            //endregion
        }catch (Exception e) {
            getLogger().info("数据库测试异常"+e.getMessage(),e);
            msg.setResult(false);
            msg.setErrorMsg("HiveMeta数据库连接异常，请核实数据库参数。");
            return msg;
        }finally {
            try {
                if (connect!=null && !connect.isClosed()){
                    connect.close();
                }
            } catch (SQLException e) {
                getLogger().error("JDBC Connection Close exception,",e);
            }
        }
    }

    private  List convertList(ResultSet rs) throws SQLException{
        List list = new ArrayList();
        ResultSetMetaData md = rs.getMetaData();//获取键名
        int columnCount = md.getColumnCount();//获取行的数量
        while (rs.next()) {
            Map rowData = new HashMap();//声明Map
            for (int i = 1; i <= columnCount; i++) {
                rowData.put(md.getColumnName(i), rs.getObject(i));//获取键名及值
            }
            list.add(rowData);
        }
        return list;
    }

    /**
     * 集群名称校验
     * @param clusterName
     * @return
     */
    private ResultMsg checkClusterName(String clusterName){
        ResultMsg msg=new ResultMsg();

        if (StringUtils.isEmpty(clusterName)){
            msg.setResult(false);
            msg.setErrorMsg("ClusterName,不能为空");
            return msg;
        }
        Pattern pattern=Pattern.compile(clusterPattern);
        Matcher matcher=pattern.matcher(clusterName);
        if (!matcher.matches()){
            msg.setResult(false);
            msg.setErrorMsg("ClusterName,不符合规则："+clusterPattern);
            return msg;
        }

        if (StrUtil.length(clusterName)>39){
            msg.setResult(false);
            msg.setErrorMsg("ClusterName,长度不能超过39");
            return msg;
        }

        msg.setResult(true);
        return msg;
    }

    /**
     * cluster版本校验
     * @param clusterReleaseVer
     * @return
     */
    private ResultMsg checkClusterReleaseVer(String clusterReleaseVer){
        ResultMsg msg=new ResultMsg();
        if (StringUtils.isEmpty(clusterReleaseVer)){
            msg.setResult(false);
            msg.setErrorMsg("clusterReleaseVer 不能为空");
            return msg;
        }

        List<BaseReleaseVersion> baseReleaseVersions=baseReleaseVersionMapper.selectAll();
        List<BaseReleaseVersion> versions=baseReleaseVersions.stream().filter(x->{
            return x.getReleaseVersion().equalsIgnoreCase(clusterReleaseVer);
        }).collect(Collectors.toList());

        if (null==versions && versions.size()==0){
            msg.setResult(false);
            List<String> version=new CopyOnWriteArrayList<>();
            baseReleaseVersions.parallelStream().forEach(x->{
                version.add(x.getReleaseVersion());
            });
            msg.setMsg("无效的version，可用的verison："+version.toArray());
            return msg;
        }
        msg.setResult(true);
        return msg;
    }


    /**
     * ambari数据库配置检查
     * @param model 数据库配置
     * @param clustername 集群名称
     * @return
     */
    private ResultMsg checkAmbariDb(DbCfgs model, String clustername){
        ResultMsg msg=new ResultMsg();
        try{
            if (StringUtils.isEmpty(model.getDatabase())){
                String database=clustername.replaceAll("-","_")+"_ambaridb";
                model.setDatabase(database);
            }
            return checkAmbariDb(model.getUrl(), model.getPort(), model.getDatabase(), model.getAccount(),
                    model.getPassword(),true);
        }catch (Exception e) {
            getLogger().info("数据库测试异常"+e.getMessage(),e);
            msg.setResult(false);
            msg.setErrorMsg("Ambari数据库连接异常，请核实数据库参数。");
            return msg;
        }

    }

    /**
     * hivemeta数据库配置检查
     *
     * @param model
     * @return
     */
    private ResultMsg checkHiveMetaDb(DbCfgs model, List<String> clusterApps) {
        ResultMsg msg = new ResultMsg();
        if (hivedbcheck.equalsIgnoreCase("false")) {
            msg.setResult(true);
            return msg;
        }
        if (clusterApps == null) {
            // extend 数据不存在，不校验hivemeta
            msg.setResult(true);
            return msg;
        }
        List<String> hives = clusterApps.stream().filter(x -> {
            return x.equalsIgnoreCase("hive");
        }).collect(Collectors.toList());
        if (hives == null || hives.size() == 0) {
            getLogger().info("不安装hive，跳过检查。");
            msg.setResult(true);
            return msg;
        }
        if (model == null) {
            msg.setResult(false);
            msg.setErrorMsg("参数hiveMetadataDbCfgs不能为空。");
            return msg;
        }
        return checkHiveMetaDb(model.getUrl(), model.getPort(), model.getDatabase(), model.getAccount(), model.getPassword());
    }

    /**
     * 检查场景参数
     * @param scene
     * @return
     */
    private ResultMsg checkScene(String scene,String releaseVerion){
        ResultMsg msg=new ResultMsg();
        if (StringUtils.isEmpty(scene)){
            msg.setResult(false);
            msg.setErrorMsg("scene,不能为空");
            return msg;
        }
        BaseScene baseScene=baseSceneMapper.queryByReleaseVerAndSceneName(releaseVerion,scene);
        if (baseScene==null){
            msg.setResult(false);
            msg.setErrorMsg("不存在的scene："+scene+",或releaseVerion不存在:"+releaseVerion);
            return msg;
        }
        msg.setResult(true);
        return msg;
    }

    /**
     * 删除保护校验
     * @param model
     * @return
     */
    private ResultMsg checkdeleteProtected(SheinRequestModel model){
        ResultMsg msg=new ResultMsg();
        if (StringUtils.isEmpty(model.getDeleteProtected())){
            msg.setResult(true);
            return msg;
        }

        if (model.getDeleteProtected().equals("1")||model.getDeleteProtected().equals("0")){
            msg.setResult(true);
            return msg;
        }else{
            msg.setResult(false);
            msg.setErrorMsg("deleteProtected 范围为：0，1");
            return msg;
        }
    }


    /**
     * shein创建接口通用
     * @param commoncolumns
     *
     *
     * @param model
     * @return
     */
    private ResultMsg checkCreateCommonString(String commoncolumns,SheinRequestModel model){
        ResultMsg msg=new ResultMsg();
        JSONObject jsonObject=JSON.parseObject(JSONObject.toJSONString(model));
        String[] cols=commoncolumns.split(",");

        for (String col:cols){
            if (StringUtils.isEmpty(jsonObject.getString(col))){
                msg.setResult(false);
                msg.setErrorMsg(col+"字段为必填。");
                return msg;
            }
        }
        msg.setResult(true);
        return msg;
    }


    private ResultMsg checkStartHa(SheinRequestModel mode){
        ResultMsg msg=new ResultMsg();
        if (mode.getStartHa()==null || (!mode.getStartHa().equals(1) && !mode.getStartHa().equals(0))){
            msg.setResult(false);
            msg.setErrorMsg("startHA 范围：0，1");
            return msg;
        }
        msg.setResult(true);
        return msg;
    }


    /**
     * 实例组数据检查
     *
     * @param instanceGroupNewConfigs
     * @return
     */
    private ResultMsg checkInstanceGroupNewConfigs(List<InstanceGroupNewConfigElement> instanceGroupNewConfigs
            , SheinRequestModel model) {
        ResultMsg msg = new ResultMsg();
        if (instanceGroupNewConfigs == null || instanceGroupNewConfigs.size() == 0) {
            msg.setResult(false);
            msg.setErrorMsg("实例组数据缺失");
            return msg;
        }
        String region=model.getDc();
        Set<String> insGpNameSet = new HashSet<>();
        int insGpNameCount = 0;
        int taskCount = 0;
        for (InstanceGroupNewConfigElement item : instanceGroupNewConfigs) {
            InstanceGroupAddConfig config = item.getInstanceGroupAddConfig();

            // region 必填验证
            if (StringUtils.isEmpty(config.getInsGpRole())
                    || StringUtils.isEmpty(config.getInsMktType())
                    || StringUtils.isEmpty(config.getVolumeType())
                    || config.getVolumeSizeInGB() == null
                    || config.getInsGpCnt() == null) {
                msg.setResult(false);
                msg.setErrorMsg("instanceGroupNewConfigs：参数字段缺失，必填字段：insGpCnt，insGpRole，" +
                        "volumeType，insMktType，volumeSizeInGB");
                return msg;
            }
            // endregion

            // region 角色有效性校验
            String[] roles = new String[]{"ambari", "master", "core", "task"};
            List<String> ss = Arrays.stream(roles).filter(x -> {
                return x.equalsIgnoreCase(config.getInsGpRole());
            }).collect(Collectors.toList());
            if (ss == null || ss.size() == 0) {
                msg.setResult(false);
                msg.setErrorMsg("insGpRole,参数错误，可选：ambari,master,core,task");
                return msg;
            }
            // endregion

            // 未传实例组名称自动补充
            if (config.getInsGpRole().equalsIgnoreCase("task")) {
                taskCount++;
            }
            if (StringUtils.isBlank(config.getInsGpName())) {
                if (!config.getInsGpRole().equalsIgnoreCase("task")) {
                    config.setInsGpName(config.getInsGpRole());
                } else {
                    String insGpNamePrefix = "task-";
                    config.setInsGpName(insGpNamePrefix + taskCount);
                }
            }

            // 实例组名称非重校验
            if (StringUtils.isNotBlank(config.getInsGpName())) {
                insGpNameSet.add(config.getInsGpName());
                insGpNameCount++;
            }

            // region 区分角色和场景校验实例数量
            if (model.getStartHa().equals(1) && config.getInsGpRole().equalsIgnoreCase("master")) {
                if (config.getInsGpCnt() != 2) {
                    msg.setResult(false);
                    msg.setErrorMsg("高可用场景，master数量需为2");
                    return msg;
                }
            }

            if (model.getStartHa().equals(0) && config.getInsGpRole().equalsIgnoreCase("master")) {
                if (config.getInsGpCnt() != 0) {
                    msg.setResult(false);
                    msg.setErrorMsg("非高可用场景，master数量需为0");
                    return msg;
                }
            }

            if (config.getInsGpRole().equalsIgnoreCase("ambari")) {
                if (config.getInsGpCnt() != 1) {
                    msg.setResult(false);
                    msg.setErrorMsg("ambari数量需为1");
                    return msg;
                }
            }

            if (config.getInsGpRole().equalsIgnoreCase("core")) {

                if (config.getInsGpCnt().compareTo(Integer.parseInt(coreNodeMinCount)) < 0
                        || config.getInsGpCnt().compareTo(Integer.parseInt(coreNodeMaxCount)) > 0) {
                    msg.setResult(false);
                    msg.setErrorMsg("core的数量范围为:" + coreNodeMinCount + "-" + coreNodeMaxCount);
                    return msg;
                }

                if (model.getScene().equalsIgnoreCase("Hbase")
                        && config.getInsGpCnt() < 3) {
                    msg.setResult(false);
                    msg.setErrorMsg("Hbase场景下，core节点至少需要3个节点");
                    return msg;
                }
            }

            if (config.getInsGpRole().equalsIgnoreCase("task")) {
                if (config.getInsGpCnt().compareTo(Integer.parseInt(taskNodeMinCount)) < 0
                        || config.getInsGpCnt().compareTo(Integer.parseInt(taskNodeMaxCount)) > 0) {
                    msg.setResult(false);
                    msg.setErrorMsg("task的数量范围为," + taskNodeMinCount + "-" + taskNodeMaxCount);
                    return msg;
                }
            }
            // endregion

            // region 数据盘大小数据校验
            ResultMsg msg1 = checkDataDiskSize(config.getVolumeSizeInGB());
            if (!msg1.getResult()) {
                return msg1;
            }
            // endregion

            // region 购买方式校验
            if (!config.getInsMktType().equalsIgnoreCase("ondemond")
                    && !config.getInsMktType().equalsIgnoreCase("spot")) {
                msg.setResult(false);
                msg.setErrorMsg("InsMktType 取值范围：ondemond/spot");
                return msg;
            }
            // endregion

            // region 数据盘类型 volumeType校验
            ResultMsg msg2 = checkDiskSku(config.getVolumeType(),region);
            if (!msg2.getResult()) {
                return msg2;
            }

            // endregion

            // region VMSKU校验
            List<String> splitSukName = sheinApiServiceImpl.splitSukName(config.getInsType());
            for (String skuName : splitSukName){
                ResultMsg msg3 = checkVMSku(skuName,region);
                if (!msg3.getResult()) {
                    return msg3;
                }
            }
            //todo 暂时不校验sku的数量, 原因:为了兼容Azure其他系统只传一个sku的情况
            // if ("spot".equalsIgnoreCase(config.getInsMktType())){
            //     //校验一下数量
            //     List<VMSku> vmSkus = metaDataItemService.listVmSkuDistinct(region, splitSukName);
            //     if (vmSkus.size() < 3 || vmSkus.size() > 15) {
            //         msg.setResult(false);
            //         msg.setErrorMsg("多机型资源池sku数量小于3个或大于15个。");
            //         return msg;
            //     }
            // }
            // endregion

            // region 数据盘数量校验 默认为 1 ，可空
            for (String skuName : splitSukName) {
                ResultMsg msg4 = checkDataDiskCnt(region,skuName,config.getDataDiskCnt());
                if (!msg4.getResult()) {
                    return msg4;
                }
            }

            // endregion

            ResultMsg msg5 = checkPurchaseType(config);
            if (!msg5.getResult()) {
                return msg5;
            }

            ResultMsg msg6 = checkProvisionType(config);
            if (!msg6.getResult()) {
                return msg6;
            }
        }

        // region 实例组重复校验
        if (insGpNameSet.size() != insGpNameCount) {
            msg.setResult(false);
            msg.setErrorMsg("实例组名称存在重复");
            return msg;
        }

        Map<String, List<InstanceGroupNewConfigElement>> map = instanceGroupNewConfigs.stream().collect(Collectors.groupingBy(item -> {
            return item.getInstanceGroupAddConfig().getInsGpRole();
        }));

        for (Map.Entry<String, List<InstanceGroupNewConfigElement>> entry : map.entrySet()) {
            if ((!entry.getValue().get(0).getInstanceGroupAddConfig().getInsGpRole().equalsIgnoreCase("task"))
                    && entry.getValue().size() > 1) {
                msg.setResult(false);
                msg.setErrorMsg("实例组角色,重复，" + entry.getKey());
                return msg;
            }
        }

        // endregion

        msg.setResult(true);
        return msg;
    }

    /**
     * 校验用户脚本数据提交数据
     * @param model
     * @return
     */
    private ResultMsg checkConfClusterScript(SheinRequestModel model){
        ResultMsg msg=new ResultMsg();
        if (model.getConfClusterScript()==null || model.getConfClusterScript().size()==0){
            msg.setResult(true);
            return msg;
        }
        List<ConfClusterScript> scriptList=model.getConfClusterScript();
        ResultMsg bolbmsg=composeMetaService.getBolbPath();
        String bolbpath=null;
        if (bolbmsg.getResult()){
            bolbpath=bolbmsg.getData().toString();
        }
        String[] runtimes=new String[]{"aftervminit","beforestart","afterstart"};
        for (ConfClusterScript script:scriptList){
            if (StringUtils.isEmpty(script.getScriptPath())
            || StringUtils.isEmpty(script.getScriptName())
            || StringUtils.isEmpty(script.getRunTiming())
            || script.getSortNo()==null){
                getLogger().info("字段缺失检查");
                msg.setResult(false);
                msg.setErrorMsg("字段缺失,必填字段：scriptName，runTiming，scriptPath，sortNo");
                return msg;
            }

            getLogger().info("insGpRole参数合法性检查。");
            List<String> ss=Arrays.stream(runtimes).filter(x->{
                return x.equalsIgnoreCase(script.getRunTiming());
            }).collect(Collectors.toList());

            if (ss==null || ss.size()==0){
                msg.setResult(false);
                msg.setErrorMsg("insGpRole参数错误,传参范围：aftervminit（实例初始化后），" +
                        "beforestart（集群启动前），afterstart（集群启动后）");
                return msg;
            }

            getLogger().info("bolbPath:"+bolbpath);
            if (StringUtils.isNotEmpty(bolbpath)){
                if (!script.getScriptPath().toLowerCase().contains(bolbpath.toLowerCase())){
                    msg.setResult(false);
                    msg.setErrorMsg("脚本存储位置不正确，正确的地址："+bolbpath);
                    return msg;
                }else{
                    try {
                        getLogger().info("地址健康检查："+script.getScriptPath());
                        boolean health=httpClient.checkhealth(script.getScriptPath());
                        if (!health){
                            msg.setResult(false);
                            msg.setErrorMsg("地址不可访问："+script.getScriptPath());
                            return msg;
                        }
                    }catch (Exception e){
                        msg.setResult(false);
                        msg.setErrorMsg("地址不可访问："+script.getScriptPath());
                        return msg;
                    }
                }
            }
        }

        Map<String,List<ConfClusterScript>> map=scriptList.stream().collect(Collectors.groupingBy(item->{
            return item.getRunTiming()+"_"+item.getSortNo();
        }));

        for (Map.Entry<String, List<ConfClusterScript>> entry:map.entrySet()){
            if (entry.getValue().size()>1){
                msg.setResult(false);
                msg.setErrorMsg("Sort_NO,重复，"+entry.getKey());
                return msg;
            }
        }
        msg.setResult(true);
        return msg;

    }


    /**
     * 校验可用区数据
     * @param model
     * @return
     */
    private ResultMsg checkAz(SheinRequestModel model){
        ResultMsg msg=new ResultMsg();
        try {
            String region=model.getDc();
            String az = model.getAz();
            AvailabilityZone zone = metaDataItemService.getAZ(region, az);
            if (zone==null){
                msg.setResult(false);
                msg.setErrorMsg("可用区不正确："+model.getAz());
                return msg;
            }
        }catch (Exception e){
            msg.setErrorMsg("可用区校验异常");
            msg.setResult(false);
            getLogger().info("可用区校验异常",e);
            return msg;
        }
        msg.setResult(true);
        return msg;
    }


    /**
     * 校验网络区域
     * @param model
     * @return
     */
    private ResultMsg checkNetWork(SheinRequestModel model){
        ResultMsg msg=new ResultMsg();
        if (!model.getvNet().equalsIgnoreCase("default-vpc")){
            msg.setResult(false);
            msg.setErrorMsg("vnet 可以范围：Default-VPC");
            return msg;
        }
        msg.setResult(true);
        return msg;
    }

    /**
     * 校验子网
     * @param model
     * @return
     */
    private ResultMsg checksubnet(SheinRequestModel model){
        ResultMsg msg=new ResultMsg();
        try {
            String region=model.getDc();
            String subnetId = model.getSubnet();
            Subnet subnet = metaDataItemService.getSubnet(region, subnetId);
            if (subnet==null) {
                msg.setResult(false);
                msg.setErrorMsg("子网不正确："+model.getSubnet());
                return msg;
            }
        }catch (Exception e){
            msg.setErrorMsg("子网校验异常");
            msg.setResult(false);
            getLogger().info("子网校验异常",e);
            return msg;
        }
        msg.setResult(true);
        return msg;
    }


    /**
     * 校验安全组
     * @param model
     * @return
     */
    private ResultMsg checkNsg(SheinRequestModel model){
        ResultMsg msg=new ResultMsg();
        try {
            String region=model.getDc();
            //主安全组
            String masterSecurityGroup = model.getMasterSecurityGroup();
            NSGSku masterNsgSku = metaDataItemService.getNSGSku(region, masterSecurityGroup);
            if (masterNsgSku==null){
                msg.setResult(false);
                msg.setErrorMsg("主安全组不正确："+model.getMasterSecurityGroup());
                return msg;
            }
            //子安全组
            String slaveSecurityGroup = model.getSlaveSecurityGroup();
            NSGSku slaveNsgSku = metaDataItemService.getNSGSku(region, slaveSecurityGroup);
            if (slaveNsgSku==null){
                msg.setResult(false);
                msg.setErrorMsg("从安全组不正确："+model.getSlaveSecurityGroup());
                return msg;
            }
        }catch (Exception e){
            msg.setErrorMsg("安全组校验异常");
            msg.setResult(false);
            getLogger().info("安全组校验异常",e);
            return msg;
        }
        msg.setResult(true);
        return msg;
    }


    /**
     * 校验密钥对
     * @param model
     * @return
     */
    private ResultMsg checkKayPair(SheinRequestModel model){
        ResultMsg msg=new ResultMsg();
        try {
            String region=model.getDc();
            String instanceKeyPair = model.getInstanceKeyPair();
            SSHKeyPair sshKeyPair = metaDataItemService.getSSHKeyPair(region, instanceKeyPair);
            if (sshKeyPair==null){
                msg.setResult(false);
                msg.setErrorMsg("SSHKey不正确："+model.getInstanceKeyPair());
                return msg;
            }
        }catch (Exception e){
            msg.setErrorMsg("SSHKey校验异常");
            msg.setResult(false);
            getLogger().info("SSHKey校验异常",e);
            return msg;
        }
        msg.setResult(true);
        return msg;
    }

    /**
     * 校验VMMI
     * @param model
     * @return
     */
    private ResultMsg checkMI(SheinRequestModel model){
        ResultMsg msg=new ResultMsg();
        try {
            String region=model.getDc();
            String resourceId = model.getVmMI();
            ManagedIdentity mi = metaDataItemService.getMI(region, resourceId);
            if (mi == null) {
                msg.setResult(false);
                msg.setErrorMsg("VMMI不正确："+model.getVmMI());
                return msg;
            }
            String logMI = model.getLogMI();
            ManagedIdentity logMi = metaDataItemService.getMI(region, logMI);
            if (logMi == null) {
                msg.setResult(false);
                msg.setErrorMsg("LOGMI不正确："+model.getLogMI());
                return msg;
            }

        }catch (Exception e){
            msg.setErrorMsg("MI校验异常");
            msg.setResult(false);
            getLogger().info("MI校验异常",e);
            return msg;
        }
        msg.setResult(true);
        return msg;
    }

    /**
     * 校验LogPath
     * @param model
     * @return
     */
    private ResultMsg checkLogPath(SheinRequestModel model){
        ResultMsg msg=new ResultMsg();
        try {
            String region=model.getDc();
            String s3LogLocation = model.getS3LogLocation();
            LogsBlobContainer logsBlobContainer = metaDataItemService.getLogsBlobContainer(region, s3LogLocation);
            if (logsBlobContainer == null) {
                msg.setResult(false);
                msg.setErrorMsg("LOG url不正确：" + model.getLogMI());
                return msg;
            }
        }catch (Exception e){
            msg.setErrorMsg("日志桶路径校验异常");
            msg.setResult(false);
            getLogger().info("日志桶路径校验异常",e);
            return msg;
        }
        msg.setResult(true);
        return msg;
    }

    /**
     * 校验clusterApps
     * @param model
     * @return
     */
    private ResultMsg checkClusterApps(SheinRequestModel model){
        ResultMsg msg=new ResultMsg();
        BaseScene baseScene=
                baseSceneMapper.queryByReleaseVerAndSceneName(model.getClusterReleaseVer(),model.getScene());
        if (baseScene==null){
            msg.setResult(false);
            msg.setErrorMsg("ClusterReleaseVer或Scene错误。");
            return msg;
        }

        List<BaseSceneApps> appsList=baseSceneAppsMapper.queryBySceneId(baseScene.getSceneId());
        if (model.getClusterApps()!=null && model.getClusterApps().size()>0){
            for (String app:model.getClusterApps()){
                Optional<BaseSceneApps> fapp= appsList.stream().filter(x->{
                    return x.getAppName().equalsIgnoreCase(app);
                }).findFirst();
                if (!fapp.isPresent()){
                    msg.setResult(false);
                    msg.setErrorMsg("当前scene中不包含的组件：" + app);
                    return msg;
                }
            }
        }
        msg.setResult(true);
        return msg;
    }

    /**
     * ganglia关联配置合法性校验
     */
    private ResultMsg checkGangliaAssociatedConf(SheinRequestModel model) {
        ResultMsg resultMsg = new ResultMsg();
        resultMsg.setResult(true);
        if (model.getEnableGanglia() == null) {
            model.setEnableGanglia(0);
            return resultMsg;
        }

        Integer enableGanglia = model.getEnableGanglia();
        if (!(enableGanglia == 0 || enableGanglia == 1)) {
            resultMsg.setResult(false);
            resultMsg.setErrorMsg("参数enableGanglia传值有误");
            return resultMsg;
        }
        Integer isEmbedAmbariDb = model.getIsEmbedAmbariDb();
        Integer ambariOsVolumeSize = model.getRootVolSize();
        List<InstanceGroupNewConfigElement> instanceGroupNewConfigs = model.getInstanceGroupNewConfigs();

        int totalVmCount = 0;
        Integer ambariDataVolumeSize = null;
        String skuName = null;
        for (InstanceGroupNewConfigElement instanceGroupNewConfig : instanceGroupNewConfigs) {
            InstanceGroupAddConfig instanceGroupAddConfig = instanceGroupNewConfig.getInstanceGroupAddConfig();
            if (instanceGroupAddConfig.getInsGpRole().equalsIgnoreCase("Ambari")) {
                ambariDataVolumeSize = instanceGroupAddConfig.getVolumeSizeInGB();
                skuName = instanceGroupAddConfig.getInsType();
            }
            Integer cnt = instanceGroupAddConfig.getInsGpCnt();
            totalVmCount += cnt;
        }

        // 启用Ganglia
        if (enableGanglia != null && enableGanglia == 1) {
            // 总实例数不能大于200
            if (totalVmCount > 200) {
                resultMsg.setResult(false);
                resultMsg.setErrorMsg("启用Ganglia后总实例数不能小于200");
                return resultMsg;
            }

            // Ambari实例的数据盘容量不能小于2T
            if (ambariDataVolumeSize != null && ambariDataVolumeSize < 2000) {
                resultMsg.setResult(false);
                resultMsg.setErrorMsg("启用Ganglia后Ambari数据盘容量不能小于2T");
                return resultMsg;
            }

            // Ambari实例的系统盘容量不能小于200G
            if (ambariOsVolumeSize != null && ambariOsVolumeSize < 200) {
                resultMsg.setResult(false);
                resultMsg.setErrorMsg("启用Ganglia后Ambari系统盘容量不能小于200G");
                return resultMsg;
            }

            // Ambari实例组规格最低为16c64G
            if (StringUtils.isBlank(skuName) || !checkEnableGangliaVmSku(skuName,model.getDc())) {
                resultMsg.setResult(false);
                resultMsg.setErrorMsg("启用Ganglia后Ambari规格最低为16c64G");
                return resultMsg;
            }
        }
        return resultMsg;
    }

    private boolean checkEnableGangliaVmSku(String skuName,String region) {
        boolean checkFlag = false;
        VMSku skus = metaDataItemService.getVMSKU(region, skuName);
        if (skus!=null){
            int vCPUs = Integer.parseInt(skus.getVCoreCount());
            float memoryGB = Float.parseFloat(skus.getMemoryGB());
            if (vCPUs >= 16 && memoryGB >= 64) {
                checkFlag = true;
            }
        }
        return checkFlag;
    }

    /**
     * 校验 tagMap
     *
     * @param model
     * @return
     */
    private ResultMsg checkTagMap(SheinRequestModel model) {
        ResultMsg msg = new ResultMsg();
        Map tagMap = model.getTagMap();
        if (tagMap != null) {
            if (tagMap.size() == 0) {
                msg.setResult(true);
                return msg;
            }

            Set tagKeySet = tagMap.keySet();
            for (Object tagKey : tagKeySet) {
                Object tagValue = tagMap.get(tagKey);
                if ((!tagKey.toString().matches(tagkeyPattern))
                        || (!tagValue.toString().matches(tagkeyPattern))) {
                    // 不符合正则规则
                    msg.setResult(false);
                    msg.setErrorMsg("标签内容不符合规则,"+tagkeyPattern);
                    return msg;
                }
            }
        }
        msg.setResult(true);
        return msg;
    }

    /**
     * 磁盘类型校验
     *
     * @param skuname
     * @return
     */
    @Override
    public ResultMsg checkDiskSku(String skuname,String region) {
        ResultMsg msg = new ResultMsg();
        try {
            DiskSku diskSku = metaDataItemService.getDiskSku(region, skuname);
            if (diskSku == null) {
                msg.setResult(false);
                msg.setErrorMsg("磁盘类型不正确："+skuname);
                return msg;
            }
        }catch (Exception e){
            msg.setErrorMsg("磁盘类型校验异常");
            msg.setResult(false);
            getLogger().info("磁盘类型校验异常",e);
            return msg;
        }
        msg.setResult(true);
        return msg;
    }

    /**
     * 数据盘大小校验
     *
     * @return
     */
    @Override
    public ResultMsg checkDataDiskSize(Integer size) {
        ResultMsg msg = new ResultMsg();
        if (size == null || size.compareTo(0) < 0) {
            msg.setResult(false);
            msg.setErrorMsg("数据盘大小不能为空且须为正整数，数据范围内：" + datadiskminsize + "，" + datadiskmaxsize);
            return msg;
        }
        Integer sizeint = size;
        Integer maxsize = Integer.parseInt(datadiskmaxsize);
        Integer minsize = Integer.parseInt(datadiskminsize);
        if (sizeint > maxsize || sizeint < minsize){
            msg.setResult(false);
            msg.setErrorMsg("数据盘大小不能为空且须为正整数，数据范围内："+datadiskminsize+"，"+datadiskmaxsize);
            return msg;
        }
        msg.setResult(true);
        return msg;
    }

    /**
     * 校验数据盘数量，结合VMSKU
     *
     * @param region
     * @param skuName
     * @param dataDiskCnt
     * @return
     */
    @Override
    public ResultMsg checkDataDiskCnt(String region,String skuName,Integer dataDiskCnt) {
        ResultMsg msg = new ResultMsg();

        if (dataDiskCnt == null) {
            msg.setResult(true);
            return msg;
        }

        if (dataDiskCnt.equals(0)) {
            msg.setResult(false);
            msg.setErrorMsg("数据盘数量最小值为：1");
            return msg;
        }
        VMSku vmsku = metaDataItemService.getVMSKU(region,  skuName);
        if (vmsku != null) {
            Integer dataDisksCount = vmsku.getMaxDataDisksCount();
            if (dataDisksCount.compareTo(dataDiskCnt) < 0) {
                // 大于VM可用磁盘数
                msg.setResult(false);
                msg.setErrorMsg("大于最大可用数量："+dataDisksCount);
                return msg;
            }
        }else{
            msg.setResult(false);
            msg.setErrorMsg("元数据异常");
            return msg;
        }
        msg.setResult(true);
        return msg;
    }

    private ResultMsg checkPurchaseType(InstanceGroupAddConfig config) {
        ResultMsg resultMsg = new ResultMsg();
        resultMsg.setResult(true);

        String insMktType = config.getInsMktType();
        Integer priceStrategy = config.getPriceStrategy();
        BigDecimal priceStrategyValue = config.getPriceStrategyValue();

        if (StringUtils.isNotBlank(insMktType)) {
            if (!(insMktType.equalsIgnoreCase("ondemond") || insMktType.equalsIgnoreCase("spot"))) {
                resultMsg.setResult(false);
                resultMsg.setErrorMsg("参数: insMktType传值有误");
                return resultMsg;
            }

            if (insMktType.equalsIgnoreCase("spot")) {
                if (priceStrategy == null || !(priceStrategy == 1 || priceStrategy == 2)) {
                    resultMsg.setResult(false);
                    resultMsg.setErrorMsg("参数: priceStrategy传值有误");
                    return resultMsg;
                }

                if (priceStrategy == 1) {  // 按市场价百分比
                    if (priceStrategyValue == null || priceStrategyValue.doubleValue() < 0 || priceStrategyValue.doubleValue() > 100) {
                        resultMsg.setResult(false);
                        resultMsg.setErrorMsg("参数: priceStrategyValue传值有误, [0,100]");
                        return resultMsg;
                    }
                } else if (priceStrategy == 2) {  // 固定价
                    if (priceStrategyValue == null || priceStrategyValue.doubleValue() < 0) {
                        resultMsg.setResult(false);
                        resultMsg.setErrorMsg("参数: priceStrategyValue传值有误, 只允许为正数");
                        return resultMsg;
                    }
                }
            }
        }
        return resultMsg;
    }

    private ResultMsg checkProvisionType(InstanceGroupAddConfig config) {
        ResultMsg resultMsg = new ResultMsg();
        resultMsg.setResult(true);

        Integer provisionType = config.getProvisionType();
        if (provisionType != null) {
            if (!(provisionType == 1 || provisionType == 2)) {
                resultMsg.setResult(false);
                resultMsg.setErrorMsg("参数: provisionType 传值有误.");
                return resultMsg;
            }
        } else {
            config.setProvisionType(1);
        }
        return resultMsg;
    }

    /**
     * 系统盘大小校验
     *
     * @return
     */
    private ResultMsg checkRootDiskSize(Integer size) {
        ResultMsg msg = new ResultMsg();
        if (null == size) {
            msg.setResult(false);
            msg.setErrorMsg("系统盘大小不能为空且须为正整数，数据范围内：" + rootdiskminsize + "，" + rootdiskmaxsize);
            return msg;
        }
        Integer maxsize=Integer.parseInt(rootdiskmaxsize);
        Integer minsize=Integer.parseInt(rootdiskminsize);
        if (size>maxsize || size<minsize){
            msg.setResult(false);
            msg.setErrorMsg("系统盘大小不能为空且须为正整数，数据范围内："+rootdiskminsize+"，"+rootdiskmaxsize);
            return msg;
        }
        msg.setResult(true);
        return msg;
    }


    /**
     * VM SKU校验
     * @param skuname
     * @return
     */
    @Override
    public ResultMsg checkVMSku(String skuname,String region){
        ResultMsg msg=new ResultMsg();
        try {
            VMSku vmsku = metaDataItemService.getVMSKU(region, skuname);
            if (vmsku == null) {
                msg.setResult(false);
                msg.setErrorMsg("VM类型不正确："+skuname);
                return msg;
            }
            msg.setData(vmsku);
        }catch (Exception e){
            msg.setErrorMsg("VM类型校验异常");
            msg.setResult(false);
            getLogger().info("VM类型校验异常",e);
            return msg;
        }
        msg.setResult(true);
        return msg;
    }

}
