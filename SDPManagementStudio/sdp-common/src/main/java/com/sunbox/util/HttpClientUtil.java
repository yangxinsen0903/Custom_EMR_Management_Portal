package com.sunbox.util;

import com.alibaba.fastjson.JSON;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class HttpClientUtil {
    private static  final Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);

    public static String doGet(String url, Map<String, String> param,Map<String,String> headerMap) {

        // 创建Httpclient对象
        CloseableHttpClient httpclient = HttpClients.createDefault();

        String resultString = "";
        CloseableHttpResponse response = null;
        try {
            // 创建uri
            URIBuilder builder = new URIBuilder(url);
            if (param != null) {
                for (String key : param.keySet()) {
                    builder.addParameter(key, param.get(key));
                }
            }
            URI uri = builder.build();

            // 创建http GET请求
            HttpGet httpGet = new HttpGet(uri);
            if (headerMap!=null && !headerMap.isEmpty()){
                for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                    httpGet.setHeader(entry.getKey(), entry.getValue());
                }
            }

            // 执行请求
            Long beg = System.currentTimeMillis();
            response = httpclient.execute(httpGet);
            Long end = System.currentTimeMillis();
            logger.info("外部接口请求耗时统计,url:{},耗时：{}ms",url,end-beg);
            // 判断返回状态是否为200
            if (response.getStatusLine().getStatusCode() == 200) {
                resultString = EntityUtils.toString(response.getEntity(), "UTF-8");
                logger.info("do get execute {} response {}", url, resultString);
            }else{
                resultString = EntityUtils.toString(response.getEntity(), "UTF-8");
                logger.info("do get execute error {}, httpstatuscode {}, response {}",
                        url,
                        response.getStatusLine().getStatusCode(),
                        resultString);
            }
        } catch (Exception e) {
            logger.error("do get execute {} exception {}", url, e);
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
                httpclient.close();
            } catch (IOException e) {
                logger.error("do get execute {} response close exception {}", url, e);
            }
        }
        return resultString;
    }

    public static String doGetWithRequestTimeOut(String url, Map<String, String> param,Map<String,String> headerMap,Integer timeout) throws Exception {

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(5000)
                .setSocketTimeout(timeout * 1000)
                .build();

        // 创建Httpclient对象
        CloseableHttpClient httpclient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();

        String resultString = "";
        CloseableHttpResponse response = null;
        try {
            // 创建uri
            URIBuilder builder = new URIBuilder(url);
            if (param != null) {
                for (String key : param.keySet()) {
                    builder.addParameter(key, param.get(key));
                }
            }
            URI uri = builder.build();

            // 创建http GET请求
            HttpGet httpGet = new HttpGet(uri);
            if (headerMap!=null && !headerMap.isEmpty()){
                for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                    httpGet.setHeader(entry.getKey(), entry.getValue());
                }
            }

            // 执行请求
            Long beg = System.currentTimeMillis();
            response = httpclient.execute(httpGet);
            Long end = System.currentTimeMillis();
            // 判断返回状态是否为200
            if (response.getStatusLine().getStatusCode() == 200) {
                resultString = EntityUtils.toString(response.getEntity(), "UTF-8");
            }
            logger.info("外部接口请求耗时统计,url:GET {},耗时：{}ms, 响应码:{}, 响应数据: {}",url,end-beg,
                    response.getStatusLine().getStatusCode(),
                    resultString);
        } catch (Exception e) {
            logger.error("do get execute {} exception {}", url, e);
            throw e;
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
                httpclient.close();
            } catch (IOException e) {
                logger.error("do get execute {} response close exception {}", url, e);
            }
        }
        return resultString;
    }

    public static String doGet(String url,Map<String,String> headerMap) {
        return doGet(url, null,headerMap);
    }

    public static String doGet(String url) {
        return doGet(url, null,null);
    }
    public static String doPost(String url, String json){
        return doPost(url, json,null);
    }

    public static String doPost(String url, String json,Map<String,String> headerMap) {
        // 创建Httpclient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String resultString = "";
        try {
            // 创建Http Post请求
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-Type", "application/json");
            // 创建请求内容
            StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
            httpPost.setEntity(entity);
            if (headerMap!=null&& !headerMap.isEmpty()){
                for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                    httpPost.setHeader(entry.getKey(), entry.getValue());
                }
            }
            // 执行http请求
            Long beg = System.currentTimeMillis();
            response = httpClient.execute(httpPost);
            Long end = System.currentTimeMillis();
            resultString = EntityUtils.toString(response.getEntity(), "UTF-8");
            logger.info("外部接口请求耗时统计,url:POST {},耗时：{}ms ,响应状态码:{}, 响应内容",url,end-beg,
                    response.getStatusLine().getStatusCode(), resultString);
        } catch (Exception e) {
            logger.error("do post execute {} exception {}", url, e);
        } finally {
            try {
                response.close();
            } catch (Exception e) {
                logger.error("do post execute {} response close exception {}", url, e);
            }
        }

        return resultString;
    }

    public static String doPut(String url, String json) {
       return doPut(url,  json,null);
    }
    public static String doPut(String url, String json,Map<String,String> headerMap) {
        // 创建Httpclient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String resultString = "";
        try {
            // 创建Http Put请求
            HttpPut httpPut = new HttpPut(url);
            if (headerMap!=null&& !headerMap.isEmpty()){
                for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                    httpPut.setHeader(entry.getKey(), entry.getValue());
                }
            }
            httpPut.setHeader("Content-Type", "application/json");
            // 创建请求内容
            StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
            httpPut.setEntity(entity);
            // 执行http请求
            Long beg = System.currentTimeMillis();
            response = httpClient.execute(httpPut);
            Long end = System.currentTimeMillis();
            resultString = EntityUtils.toString(response.getEntity(), "UTF-8");
            logger.info("外部接口请求耗时统计,url:PUT {},耗时：{}ms, 状态码:{}, 响应内容:{}",url,end-beg,
                    response.getStatusLine().getStatusCode(),
                    resultString);
        } catch (Exception e) {
            logger.error("do put execute: {}, exception: {}", url, e);
        } finally {
            try {
                response.close();
            } catch (Exception e) {
                logger.error("do put execute: {}, response close exception: {}", url, e);
            }
        }
        return resultString;
    }

    public static HttpResult httpPut(String url, String json,Map<String,String> headerMap) {
        // 创建Httpclient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String resultString = "";
        Integer statusCode = null;
        try {
            // 创建Http Put请求
            HttpPut httpPut = new HttpPut(url);
            if (headerMap!=null&& !headerMap.isEmpty()){
                for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                    httpPut.setHeader(entry.getKey(), entry.getValue());
                }
            }
            httpPut.setHeader("Content-Type", "application/json");
            // 创建请求内容
            StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
            httpPut.setEntity(entity);
            // 执行http请求
            Long beg = System.currentTimeMillis();
            response = httpClient.execute(httpPut);
            Long end = System.currentTimeMillis();
            statusCode = response.getStatusLine().getStatusCode();
            resultString = EntityUtils.toString(response.getEntity(), "UTF-8");
            logger.info("外部接口请求耗时统计,url:PUT {},耗时：{}ms, 响应码:{}, 响应内容: {}",url,end-beg,
                    response.getStatusLine().getStatusCode(),
                    resultString);
            return new HttpResult(statusCode, resultString);
        } catch (Exception e) {
            logger.error("do put execute: {}, exception: {}", url, e);
            if (statusCode == null) {
                return new HttpResult(HttpResult.EXCEPTION_CODE, e.getMessage());
            } else {
                return new HttpResult(statusCode, e.getMessage());
            }
        } finally {
            try {
                response.close();
            } catch (Exception e) {
                logger.error("do put execute: {}, response close exception: {}", url, e);
            }
        }
    }

    /**
     *
     *
     * @param url
     * @param json
     * @param timeout
     * @return
     */
    public static HttpResult httpPutWithRequestTimeOut(String url, String json,Map<String,String> headerMap,Integer timeout) {

        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(timeout * 1000)
                .setConnectTimeout(5000)
                .build();
        // 创建Httpclient对象
        CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
        CloseableHttpResponse response = null;
        String resultString = "";
        Integer statusCode = null;
        try {
            // 创建Http Put请求
            HttpPut httpPut = new HttpPut(url);
            if (headerMap!=null && !headerMap.isEmpty()){
                for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                    httpPut.setHeader(entry.getKey(), entry.getValue());
                }
            }
            httpPut.setHeader("Content-Type", "application/json");
            // 创建请求内容
            StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
            httpPut.setEntity(entity);
            // 执行http请求
            Long beg = System.currentTimeMillis();
            response = httpClient.execute(httpPut);
            Long end = System.currentTimeMillis();
            statusCode = response.getStatusLine().getStatusCode();
            resultString = EntityUtils.toString(response.getEntity(), "UTF-8");
            logger.info("外部接口请求耗时统计,url:{},耗时：{}ms, 响应码:{}, 响应内容: {}",url,end-beg,
                    response.getStatusLine().getStatusCode(),
                    resultString);
            return new HttpResult(statusCode, resultString);
        } catch (Exception e) {
            logger.error("do put execute: {}, exception: {}", url, e);
            if (statusCode == null) {
                return new HttpResult(HttpResult.EXCEPTION_CODE, e.getMessage());
            } else {
                return new HttpResult(statusCode, e.getMessage());
            }
        } finally {
            try {
                response.close();
            } catch (Exception e) {
                logger.error("do put execute: {}, response close exception: {}", url, e);
            }
        }
    }


    /**
     *
     * post with multipart file,file name in url
     * */
    public static String doPostWithMultipartFile(String url, MultipartFile file,Map<String,String> headerMap) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        String respStr = "";
        try {
            HttpPost httpPost = new HttpPost(url);
            if (headerMap!=null && !headerMap.isEmpty()){
                for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                    httpPost.setHeader(entry.getKey(), entry.getValue());
                }
            }
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            String filename = file.getOriginalFilename();
            builder.addBinaryBody("file", file.getBytes(), ContentType.MULTIPART_FORM_DATA, filename);
            // 如果需要，传递额外参数
            // StringBody fileName = new StringBody("文件名称", ContentType.MULTIPART_FORM_DATA);
            // StringBody userName = new StringBody("用户名", ContentType.MULTIPART_FORM_DATA);
            // builder.addPart("fileName", fileName);
            // builder.addPart("userName", userName);

            HttpEntity entity = builder.build();
            httpPost.setEntity(entity);
            CloseableHttpResponse response = httpClient.execute(httpPost);
            respStr = EntityUtils.toString(response.getEntity(), "UTF-8");
            logger.info("doPostWithMultipartFile req uri {}  respStr {}", url, respStr);
        } catch (Exception e) {
            logger.error("doPostWithMultipartFile req url {} catch exception {}", url, e);
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                logger.error("doPostWithMultipartFile req close response url {} catch exception {}", url, e);
            }
        }
        return respStr;
    }


    /**
     * http delete request without json
     * @param url
     * */
    public static String doDelete(String url,Map <String,String> headerMap) {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpDelete httpDelete = new HttpDelete(url);
        if (headerMap!=null&& !headerMap.isEmpty()){
            for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                httpDelete.setHeader(entry.getKey(), entry.getValue());
            }
        }

        CloseableHttpResponse response = null;
        String resultString = "";
        try {
            response = httpclient.execute(httpDelete);
            logger.info("删除集群信息,httpcode:"+response.getStatusLine().getStatusCode()+"");
            resultString = EntityUtils.toString(response.getEntity());
            if (response.getStatusLine().getStatusCode()!=200){
                if (response.getStatusLine().getStatusCode()==404) {
                    Map<String, Object> msg = new HashMap<>();
                    msg.put("id", "000000");
                    msg.put("data", resultString);
                    msg.put("httpcode", response.getStatusLine().getStatusCode());
                    resultString = JSON.toJSONString(msg);
                }
            }
            logger.info("doDelete request response url {} data {}", url, resultString);
        } catch (IOException e) {
            logger.error("doDelete request catch exception {}", e);
        } finally {
            if(null != response) {
                try {
                    response.close();
                } catch (IOException e) {
                    logger.error("doDelete request close response catch exception {}", e);
                }
            }
        }
        return resultString;
    }


    public static String doDeleteWithRequestTimeOut(String url,Map<String,String> headerMap,Integer timeOut) throws Exception {
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(timeOut * 1000)
                .setConnectTimeout(5000)
                .build();
        CloseableHttpClient httpclient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();;
        HttpDelete httpDelete = new HttpDelete(url);
        if (headerMap!=null&& !headerMap.isEmpty()){
            for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                httpDelete.setHeader(entry.getKey(), entry.getValue());
            }
        }
        CloseableHttpResponse response = null;
        String resultString = "";
        try {
            response = httpclient.execute(httpDelete);
            logger.info("删除请求,httpcode:"+response.getStatusLine().getStatusCode()+"");
            resultString = EntityUtils.toString(response.getEntity());
            if (response.getStatusLine().getStatusCode()!=200){
                if (response.getStatusLine().getStatusCode()==404) {
                    Map<String, Object> msg = new HashMap<>();
                    msg.put("id", "000000");
                    msg.put("data", resultString);
                    msg.put("httpcode", response.getStatusLine().getStatusCode());
                    resultString = JSON.toJSONString(msg);
                }
            }
            logger.info("doDelete request response url {} data {}", url, resultString);
        } catch (IOException e) {
            logger.error("doDelete request catch exception {}", e);
            throw e;
        } catch (Exception e){
            logger.error("doDelete request catch exception {}", e);
            throw e;
        }finally {
            if(null != response) {
                try {
                    response.close();
                } catch (IOException e) {
                    logger.error("doDelete request close response catch exception {}", e);
                    throw e;
                }
            }
        }
        return resultString;
    }

    public static class HttpResult{
        public static final int EXCEPTION_CODE = 4;
        private int statusCode;
        private String responseBody;

        public HttpResult(int statusCode, String responseBody) {
            this.statusCode = statusCode;
            this.responseBody = responseBody;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getResponseBody() {
            return responseBody;
        }
    }
}
