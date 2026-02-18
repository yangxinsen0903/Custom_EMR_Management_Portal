package com.sunbox.util;


import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.net.URLEncoder;
import java.util.Map;
import java.util.Map.Entry;


public class HttpUtilApp {

    private static Logger logger = Logger.getLogger(HttpUtilApp.class);


    /**
     * 获取HttpGet实例
     *
     * @param url    请求地址
     * @param object 请求实例
     * @return
     */
    private static HttpGet getHttpGetMethod(String url, Object object) {
        HttpGet get = new HttpGet(wrapUrlWithBean(url, object));
        return get;
    }


    /**
     * 获取HttpPost实例
     *
     * @param url    请求地址
     * @param object 请求实例
     * @return
     */
    private static HttpPost getHttpPostMethod(String url, Object object) {
        HttpPost post = new HttpPost(wrapUrlWithBean(url, object));
        return post;
    }


    /**
     * 包装url地址
     *
     * @param url    原始的url地址
     * @param object 把一个对象转换成健值对的形式，拼接到url地址上 <br />
     *               如：http://wwww.google.com?username=zhangsan&age=100 <br />
     *               url = http://wwww.google.com<br />
     *               object{private String username;private String age;}
     * @return
     */
    private static String wrapUrlWithBean(String url, Object object) {
        if (StringUtils.isBlank(url))
            return null;
        Map<String, String> map = (Map<String, String>) object;
        if (map == null || map.isEmpty()) {
            return url;
        }
        StringBuffer sb = new StringBuffer(url);
        for (Entry<String, String> entry : map.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }
            if (sb.indexOf("?") != -1) {
                sb.append("&");
            } else {
                sb.append("?");
            }
            try {
                sb.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue().toString(), "utf-8"));
            } catch (Exception e) {
                e.printStackTrace();
                logger.error(e);
                return url;
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("============request url " + sb + "==============");
        }
        return sb.toString();
    }

    /**
     * 获取HttpClient实例
     */
    private static CloseableHttpClient getHttpClient() {
        return HttpClients.createDefault();
    }

    /**
     * 把HttpResponse的实例转换成字符
     */
    private static String getEntityString(HttpResponse response) {
        try {
            if (response == null || response.getEntity() == null)
                return null;
            return EntityUtils.toString(response.getEntity());
        } catch (Exception e) {
            logger.warn(e);
            return null;
        }
    }

    /**
     * 执行HttpPost方法
     *
     * @param url    路径
     * @param object
     * @return
     */
    public static String doPost(String url, Object object) {
        String urls =wrapUrlWithBean(url, object);
        logger.info("http请求参数==="+urls);
        String json= "";
        try {
            json= httpClient.request(urls);
//            logger.info("http返回结果==="+json);
        }catch (Exception e){

        }
       /* HttpPost post = getHttpPostMethod(url, object);
        CloseableHttpClient client = getHttpClient();
        CloseableHttpResponse response = null;
        String json = null;
        try {
            response = client.execute(post);
            if (response != null) {
                json = getEntityString(response);
            }
        } catch (Exception e) {
            logger.warn(e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (response != null) {
                    response.close();
                    response = null;
                }
                if (client != null) {
                    client.close();
                    client = null;
                }
            } catch (Exception e) {
                logger.warn(e);
                return null;
            }
        }*/

        return json;
    }

    /**
     * 执行HttpGet
     *
     * @param url    地址
     * @param object 请求参数实例
     * @return
     */
    public static String doGet(String url, Object object) {

        HttpGet get = getHttpGetMethod(url, object);

        CloseableHttpClient client = getHttpClient();

        CloseableHttpResponse response = null;

        String json = null;
        try {
            response = client.execute(get);
            if (response != null) {
                json = getEntityString(response);
            }
        } catch (Exception e) {
            logger.warn(e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (response != null) {
                    response.close();
                    response = null;
                }
                if (client != null) {
                    client.close();
                    client = null;
                }
            } catch (Exception e) {
                logger.warn(e);
                return null;
            }
        }

        return json;
    }
}
