package com.azure.csu.tiger.rm.base.task;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class UploadLogTask {

    private static final Logger logger = LoggerFactory.getLogger(UploadLogTask.class);

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${custom.log.upload.url}")
    private String logUploadUrl;

    @Value("${custom.log.root}")
    private String logRoot;
    @Value("${custom.log.upload.storageAccount}")
    private String storageAccount;
    @Value("${custom.log.upload.container}")
    private String container;
    @Value("${azure.default-subscriptionId}")
    private String subscriptionId;

    private HttpClient httpClient;

    @PostConstruct
    private void init() {
        httpClient = HttpClients.createDefault();
    }

//    @Scheduled(cron = "5 */2 * * * *")
    @Scheduled(cron = "5 2 * * * *")
    public void upLoadLogToBlob(){
        try {
            int randomDelay = ThreadLocalRandom.current().nextInt(0, 10 * 60 * 1000);
            logger.info("wait random time: " + randomDelay + "ms");
            Thread.sleep(randomDelay);

            logger.info("开始上传日志");
            LocalDateTime logdate = oneHourAgoDateTime();
            File file = getLogFile(logdate);
            logger.info("日志文件地址："+file.getAbsolutePath());
            Integer i = 0;
            while (true) {
                if (file.exists()) {
                    try {
                        String url = getLogUploadUrl(logdate);
                        String responseStr = doPostWithFile(url, file);
                        logger.info("上传返回报文："+responseStr);
                        break;
                    }catch (Exception e){
                        logger.error("上传文件异常，",e);
                    }
                }else{
                    logger.error("文件不存在："+file.getAbsolutePath());
                }
                i ++;
                if (i > 5){
                    break;
                }
                Thread.sleep(1000 * 60 * 2);
            }
        }catch (Exception e){
            logger.error("");
        }

    }

    private LocalDateTime oneHourAgoDateTime() {
        // 获取当前时间
        LocalDateTime currentTime = LocalDateTime.now();
        // 减去1小时
        LocalDateTime oneHourBefore = currentTime.minus(1, ChronoUnit.HOURS);
        return oneHourBefore;
    }

    private File getLogFile(LocalDateTime logDate){
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HH");
            String sdfdate = logDate.format(formatter);
            String logFileName = applicationName + "_" + sdfdate + ".zip";
            String logFilePath = String.format("%s/%s", logRoot, logFileName);
            return new File(logFilePath);
        }catch (Exception e){
            logger.error("获取logFilePath,异常",e);
            throw new RuntimeException("获取logFilePath,异常");
        }
    }

    private String getLogUploadUrl(LocalDateTime logDate){
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            String sdfdate = logDate.format(formatter);
            if (!StringUtils.hasText(logUploadUrl)){
                throw new RuntimeException("please config custom.log.upload.url");
            }
            String upLoadPath = String.format("%s/api/v1/blobs/logs/%s/%s/%s/%s/%s", logUploadUrl, storageAccount, container, applicationName, sdfdate, getUpLoadLogFileName(logDate));
            logger.info("上传地址："+upLoadPath);
            return upLoadPath;
        }catch (Exception e){
            logger.error("获取UploadUrl,异常",e);
            throw new RuntimeException("获取UploadUrl,异常");
        }
    }

    private String getUpLoadLogFileName(LocalDateTime logDate){
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HH");
            String sdfdate = logDate.format(formatter);
            String hostName = getHostName();
            String logFileName = applicationName + "-" +hostName+ "-" + sdfdate + ".zip";
            return logFileName;
        }catch (Exception e){
            logger.error("获取logFilePath,异常",e);
            throw new RuntimeException("获取logFilePath,异常");
        }
    }

    private String getHostName(){
        try {
            InetAddress inetadd = InetAddress.getLocalHost();
            String name = inetadd.getHostName();
            return name;
        }
        catch(Exception u){
            return null;
        }
    }

    public String doPostWithFile(String url, File file) {
        HttpPost request = new HttpPost(url);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addBinaryBody("file", file, ContentType.MULTIPART_FORM_DATA, file.getName());
        request.setEntity(builder.build());
        request.addHeader("subscriptionId", subscriptionId);
        try {
            return httpClient.execute(request,
                    response -> {
                        String responseBody = EntityUtils.toString(response.getEntity());
                        logger.info("upload log file, Request url: {}, Response: {}", url, responseBody);
                        return responseBody;
                    });
        } catch (Exception e) {
            logger.error("upload log file error, Request url: {}", url, e);
        }
        return null;
    }

}
