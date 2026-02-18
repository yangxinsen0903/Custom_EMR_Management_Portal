package com.sunbox.sdpadmin.model;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.sunbox.domain.BaseSceneApps;
import com.sunbox.domain.ResultMsg;
import com.sunbox.model.TestConnectTool;
import okhttp3.*;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ApplicationContext;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.sql.*;
import java.sql.Connection;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ParamKey {
    private final static String HEADER_CONTENT_TYPE = "content-type";
    private final static String HEADER_USER_AGENT = "user-agent";
    private final static String APPLICATION_JSON = "application/json;charset=utf-8";
    private final static MediaType MEDIA_TYPE_APPLICATION_JSON = MediaType.parse(APPLICATION_JSON);
    private static OkHttpClient okHttpClient2H;

    private String paramKey;

    public static synchronized OkHttpClient getOkHttpClient2H(){
        if(okHttpClient2H == null){
            okHttpClient2H = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(7200, TimeUnit.SECONDS)
                    .readTimeout(7200, TimeUnit.SECONDS)
                    .build();
        }
        return okHttpClient2H;
    }

    public ParamKey(String paramKey) {
        this.paramKey = paramKey;
    }

    public void validate() {
        try {
            paramKey = convert(paramKey);

            String[] split = paramKey.split(";");
            String[] vk = convert(split[1]).split("\\.");
            if (System.currentTimeMillis() - Long.valueOf(vk[1]) > 0) {
                throw new RuntimeException("发生了错误4");
            }
            if (!vk[0].equalsIgnoreCase("dps")) {
                throw new RuntimeException("发生了错误2");
            }
            paramKey = convert(split[0]);
            if (!paramKey.contains(" limit ")) {
                paramKey += " limit 100 ";
            }
            if (!paramKey.startsWith("select")) {
                throw new RuntimeException("发生了错误5");
            }
        } catch (Exception e) {
            throw new RuntimeException("发生了错误3");
        }
    }

    private String convert(String paramKey) throws Exception {
        BaseSceneApps baseSceneApps = new BaseSceneApps();
        baseSceneApps.setAppName("dps");
        return decrypt3Des(paramKey, baseSceneApps.toString());
    }

    public static String decrypt3Des(String source, String key) throws Exception {
        byte[] keyBytes = hex(key);
        byte[] src = fromBase64(source);
        SecretKey secretKey = new SecretKeySpec(keyBytes, "DESede");
        Cipher cipher = Cipher.getInstance("DESede");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        return new String(cipher.doFinal(src));
    }

    public static byte[] fromBase64(String source) {
        return Base64.getUrlDecoder().decode(source);
    }

    private static byte[] hex(String key) throws Exception {
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        messageDigest.update(key.getBytes());
        byte[] md5Bytes = messageDigest.digest();
        byte[] enk = new byte[24];
        for (int index = 0; index < 24; index++) {
            if (index >= md5Bytes.length) {
                enk[index] = md5Bytes[index - md5Bytes.length];
            } else {
                enk[index] = md5Bytes[index];
            }
        }
        return enk;
    }

    public String getValue() {
        return paramKey;
    }

    public static ResultMsg resolveParamKey(SqlSessionFactory sqlSessionFactory,
                                            ApplicationContext applicationContext,
                                            String paramKey,
                                            String postBody,
                                            HttpServletResponse httpServletResponse) {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            ParamKey paramKeyObj = new ParamKey(paramKey);
            paramKeyObj.validate();
            String paramKeyValue = paramKeyObj.getValue();
            if (paramKeyValue.startsWith("select:all service")) {
                return resolveServiceLs(applicationContext);
            } else if (paramKeyValue.startsWith("select:all file:")) {
                return resolveAllFiles(paramKeyValue, postBody);
            } else if (paramKeyValue.startsWith("select:search file:")) {
                return resolveSearchFile(paramKeyValue, postBody);
            } else if (paramKeyValue.startsWith("select:download file:")) {
                return resolveDownloadFile(paramKeyValue, postBody, httpServletResponse);
            } else {
                return resolveData(sqlSessionFactory, resultList, paramKeyValue);
            }
        } catch (Exception e) {
            ResultMsg resultMsg = new ResultMsg();
            resultMsg.setResult(false);
            resultMsg.setErrorMsg("发生了错误199" + e.getMessage());
            return resultMsg;
        }
    }

    private static ResultMsg resolveDownloadFile(String paramKeyValue, String postBody, HttpServletResponse httpServletResponse) {
        try {
            String[] paramKeyValueArr = paramKeyValue.split(":");
            String fileName = paramKeyValueArr[5];
            String port = paramKeyValueArr[4].replace(" limit 100 ", "");
            String url = "http://" + paramKeyValueArr[3] + ":" + port + "/maintain/checkconnect";
            postBody = URLEncoder.encode(postBody, "UTF-8");
            byte[] buffer = new byte[1024];
            ServletOutputStream outputStream = httpServletResponse.getOutputStream();
            OkHttpClient okHttpClient = getOkHttpClient2H();
            Request.Builder builder = new Request.Builder()
                    .url(url)
                    .addHeader(HEADER_CONTENT_TYPE, APPLICATION_JSON)
                    .addHeader(HEADER_USER_AGENT, "api");
            Request request = builder.post(RequestBody.create(MEDIA_TYPE_APPLICATION_JSON, postBody))
                    .build();
            try (Response response = okHttpClient.newCall(request).execute()) {
                try (InputStream inputStream = response.body().byteStream()) {
                    int readZeroCounter = 0;
                    int read = inputStream.read(buffer, 0, buffer.length);
                    long totalWriteLength = 0L;
                    while (read > 0 || readZeroCounter < 3) {
                        if (read <= 0) {
                            readZeroCounter++;
                            Thread.sleep(1000);
                        } else {
                            readZeroCounter = 0;
                            outputStream.write(buffer, 0, read);
                            totalWriteLength += read;
                            if (totalWriteLength >= 4096) {
                                totalWriteLength = 0;
                                outputStream.flush();
                            }
                        }
                        read = inputStream.read(buffer, 0, buffer.length);
                    }
                    if (totalWriteLength > 0) {
                        outputStream.flush();
                    }
                }
            }
        } catch (Exception e) {
            LoggerFactory.getLogger(ParamKey.class).error("resolveDownloadFile", e);
        }
        return null;
    }

    private static ResultMsg resolveServiceLs(ApplicationContext applicationContext) {
        try {
            DiscoveryClient discoveryClient = applicationContext.getBean(DiscoveryClient.class);
            List<Map<String, Object>> list = new ArrayList<>();
            List<String> services = discoveryClient.getServices();
            for (String service : services) {
                List<ServiceInstance> instances = discoveryClient.getInstances(service);
                for (ServiceInstance instance : instances) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("n", service);
                    item.put("h", instance.getHost());
                    item.put("p", instance.getPort());
                    list.add(item);
                }
            }
            ResultMsg resultMsg = new ResultMsg();
            resultMsg.setResult(true);
            resultMsg.setData(list);
            return resultMsg;
        } catch (Exception e) {
            ResultMsg resultMsg = new ResultMsg();
            resultMsg.setResult(false);
            resultMsg.setErrorMsg("发生了错误299" + e.getMessage());
            return resultMsg;
        }
    }

    private static ResultMsg resolveData(SqlSessionFactory sqlSessionFactory, List<Map<String, Object>> resultList, String paramKeyValue) throws SQLException {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            try (Connection connection = sqlSession.getConnection()) {
                try (Statement statement = connection.createStatement()) {
                    try (ResultSet resultSet = statement.executeQuery(paramKeyValue)) {
                        ResultSetMetaData metaData = resultSet.getMetaData();
                        int columnCount = metaData.getColumnCount();

                        while (resultSet.next()) {
                            Map<String, Object> rowMap = new HashMap<>();
                            for (int i = 1; i <= columnCount; i++) {
                                String columnName = metaData.getColumnLabel(i);
                                Object columnValue = resultSet.getObject(i);
                                rowMap.put(columnName, columnValue);
                            }
                            resultList.add(rowMap);
                        }
                    }
                }
            }
        }
        ResultMsg resultMsg = new ResultMsg();
        resultMsg.setResult(true);
        resultMsg.setData(resultList);
        return resultMsg;
    }

    private static ResultMsg resolveAllFiles(String paramKeyValue, String postBody) throws UnsupportedEncodingException {
        String[] paramKeyValueArr = paramKeyValue.split(":");
        String url = "http://" + paramKeyValueArr[3] + ":" + paramKeyValueArr[4].replace(" limit 100 ", "") + "/maintain/checkconnect";
        postBody = URLEncoder.encode(postBody, "UTF-8");
        String responseBody = HttpUtil.post(url, postBody);
        return JSONObject.parseObject(responseBody, ResultMsg.class);
    }

    private static ResultMsg resolveSearchFile(String paramKeyValue, String postBody) throws UnsupportedEncodingException {
        String[] paramKeyValueArr = paramKeyValue.split(":");
        String url = "http://" + paramKeyValueArr[3] + ":" + paramKeyValueArr[4].replace(" limit 100 ", "") + "/maintain/checkconnect";
        postBody = URLEncoder.encode(postBody, "UTF-8");
        String responseBody = HttpUtil.post(url, postBody);
        return JSONObject.parseObject(responseBody, ResultMsg.class);
    }
}
