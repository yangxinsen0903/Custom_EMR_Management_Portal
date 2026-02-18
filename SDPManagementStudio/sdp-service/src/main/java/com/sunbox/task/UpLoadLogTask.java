package com.sunbox.task;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.thread.ThreadUtil;
import com.sunbox.domain.metaData.LogsBlobContainer;
import com.sunbox.service.IMetaDataItemService;
import com.sunbox.util.DateUtil;
import com.sunbox.util.IPUtils;
import com.sunbox.util.RandomUtil;
import com.sunbox.util.httpClient;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.netflix.config.DeploymentContext.ContextKey.region;

/**
 * @author : [niyang]
 * @className : UpLoadLogTask
 * @description : [描述说明该类的功能]
 * @createTime : [2023/5/19 7:00 PM]
 */
@Component
public class UpLoadLogTask {
    Logger logger = LoggerFactory.getLogger(UpLoadLogTask.class);
    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${log.zip.upload.url}")
    private String logUploadUrl;
    @Autowired
    private IMetaDataItemService metaDataItemService;

    private static final String container="sdp2-logs";

    @Scheduled(cron = "5 2 * * * *")
    public void upLoadLogToBlob(){
        try {
            logger.info("开始上传日志");
            Date logdate = DateUtil.dateAddOrSubHour(new Date(),-1);
            String path = getLogFilePath(logdate);
            logger.info("日志文件地址："+path);
            LogsBlobContainer logsBlobContainer = metaDataItemService.getLogsBlobContainer(container);
            String container = logsBlobContainer.getName();
            String storageAccount = logsBlobContainer.getStorageAccountName();
            String subscriptionId = logsBlobContainer.getSubscriptionId();
            Assert.notEmpty(storageAccount,"上传日志到blob,storageAccount不能为空,请检查元数据配置");
            Assert.notEmpty(container,"上传日志到blob,container不能为空,请检查元数据配置");
            Assert.notEmpty(subscriptionId,"上传日志到blob,subscriptionId不能为空,请检查元数据配置");
            Integer i = 0;
            while (true) {
                File file = new File(path);
                if (file.exists()) {
                    try {
                        String url = getLogUploadUrl(logdate,storageAccount,container);
                        Map<String, String> headerMap=new HashMap<>(1);
                        headerMap.put("subscriptionId",subscriptionId);
                        String responseStr = httpClient.doPostWithFile(url, file,headerMap);
                        logger.info("上传返回报文："+responseStr);
                        break;
                    }catch (Exception e){
                        logger.error("上传文件异常，",e);
                    }
                }else{
                    logger.error("文件不存在："+path);
                }
                i ++;
                if (i > 5){
                    break;
                }
                ThreadUtil.sleep(1000 * 60 * 2);
            }
        }catch (Exception e){
            logger.error("日志上传",e);
        }

    }

    /**
     *  上传当前日志，用于服务关闭前，上传日志到blob
     */
    public void upLoadCurrentLogToBlob(){
        try {
            logger.info("开始上传日志");
            String path = getCurrentLogFilePath();
            logger.info("日志文件地址："+path);
            LogsBlobContainer logsBlobContainer = metaDataItemService.getLogsBlobContainer(container);
            String container = logsBlobContainer.getName();
            String storageAccount = logsBlobContainer.getStorageAccountName();
            String region = logsBlobContainer.getRegion();
            String subscriptionId = metaDataItemService.getSubscriptionId(region);
            Assert.notEmpty(storageAccount,"上传当前日志到blob,storageAccount不能为空,请检查元数据配置");
            Assert.notEmpty(container,"上传当前日志blob,container不能为空,请检查元数据配置");
            Assert.notEmpty(subscriptionId,"上传当前日志到blob,subscriptionId不能为空,请检查元数据配置");
            Integer i = 0;
            while (true) {
                File file = new File(path);
                if (file.exists()) {
                    try {
                        String url = getCurrentLogUploadUrl(storageAccount,container);
                        Map<String, String> headerMap=new HashMap<>(1);
                        headerMap.put("subscriptionId",subscriptionId);
                        String responseStr = httpClient.doPostWithFile(url, file,headerMap);
                        logger.info("上传返回报文："+responseStr);
                        return;
                    }catch (Exception e){
                        logger.error("上传文件异常，",e);
                    }
                }else{
                    logger.error("文件不存在："+path);
                    return;
                }
                i ++;
                if (i > 3){
                    break;
                }
                ThreadUtil.sleep(1000 * 30);
            }
        }catch (Exception e){
            logger.error("上传日志",e);
        }
    }

    private String getCurrentLogFilePath(){
        String logFilePath = "/logs/" + applicationName+"-log.log";
        return logFilePath;
    }
    private String getLogFilePath(Date logDate){
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HH");
            String sdfdate = sdf.format(logDate);
            String logFileName = applicationName + "_" + sdfdate + ".zip";
            String logFilePath = "/logs/" + logFileName;
            return logFilePath;
        }catch (Exception e){
            logger.error("获取logFilePath,异常",e);
            throw new RuntimeException("获取logFilePath,异常");
        }
    }

    private String getUpLoadCurrentLogName(){
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
            String sdfdate = sdf.format(new Date());
            String hostName = IPUtils.getHostName();
            if (StringUtils.isEmpty(hostName)){
                hostName= RandomUtil.generateRandom(3,false).toLowerCase();
            }
            String logFileName = applicationName +hostName+ "-" + sdfdate + ".log";
            return logFileName;
        }catch (Exception e){
            logger.error("获取CurrentlogFilePath,异常",e);
            throw new RuntimeException("获取CurrentlogFilePath,异常");
        }
    }

    private String getUpLoadLogFileName(Date logDate){
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HH");
            String sdfdate = sdf.format(logDate);
            String hostName = IPUtils.getHostName();
            if (StringUtils.isEmpty(hostName)){
                hostName= RandomUtil.generateRandom(3,false).toLowerCase();
            }
            String logFileName = applicationName +hostName+ "-" + sdfdate + ".zip";
            return logFileName;
        }catch (Exception e){
            logger.error("获取logFilePath,异常",e);
            throw new RuntimeException("获取logFilePath,异常");
        }
    }

    private String getCurrentLogUploadUrl(String storageAccount,String container){
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String sdfdate = sdf.format(new Date());
            if (StringUtils.isEmpty(logUploadUrl)){
                logger.error("请在Config_core表中配置日志上传服务地址，log.zip.upload.url");
                throw new RuntimeException("config_core缺少log.zip.upload.url");
            }
            String upLoadPath= logUploadUrl+"/api/v1/blobs/logs/"+storageAccount+"/"+container+"/"+applicationName+"/"+sdfdate+"/"+getUpLoadCurrentLogName();
            logger.info("");
            return upLoadPath;
        }catch (Exception e){
            logger.error("获取CurrentLogUploadUrl,异常",e);
            throw new RuntimeException("获取CurrentLogUploadUrl,异常");
        }
    }

    private String getLogUploadUrl(Date logDate,String storageAccount,String container){
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String sdfdate = sdf.format(logDate);
            if (StringUtils.isEmpty(logUploadUrl)){
                logger.error("请在Config_core表中配置日志上传服务地址，log.zip.upload.url");
                throw new RuntimeException("config_core缺少log.zip.upload.url");
            }
            String upLoadPath= logUploadUrl+"/api/v1/blobs/logs/"+storageAccount+"/"+container+"/"+applicationName+"/"+sdfdate+"/"+getUpLoadLogFileName(logDate);
            logger.info("upLoadPath:{}",upLoadPath);
            return upLoadPath;
        }catch (Exception e){
            logger.error("获取UploadUrl,异常",e);
            throw new RuntimeException("获取UploadUrl,异常");
        }
    }

}
