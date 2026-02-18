package sunbox.sdp.ambari.client;


import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.sunbox.util.JsonMapper;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/*
Created BY niyang
Created Date 2015/11/18
*/
public class HttpClient {
    private static final Logger logger = LoggerFactory.getLogger("httpClient");

    public static String request(String path,Object json) throws  Exception {
        String result="";
        String encoding="UTF-8";
        String params =new JsonMapper().toJson(json);
        byte[] data = params.getBytes(encoding);
        URL url =new URL(path);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json; charset="+ encoding);
        conn.setRequestProperty("Content-Length", String.valueOf(data.length));
        conn.setConnectTimeout(90*1000);
        conn.setReadTimeout(90*1000);
        OutputStream outStream = conn.getOutputStream();
        outStream.write(data);
        outStream.flush();
        outStream.close();
        if(conn.getResponseCode()==200){
            InputStream inStream = conn.getInputStream();
            result=new String(readStream(inStream));
        }
        return result;
    }

    public static String request(String path,String method,Object json) throws  Exception {
        Boolean flag=false;
        if (json!=null){
            flag=true;
        }
        String result="";
        String encoding="UTF-8";
        String params ="";
        if(flag) {
            params = new JsonMapper().toJson(json);
        }
        byte[] data = params.getBytes(encoding);
        URL url =new URL(path);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod(method);
        conn.setDoOutput(true);
        if(flag) {
            conn.setRequestProperty("Content-Type", "application/json; charset=" + encoding);
            conn.setRequestProperty("Content-Length", String.valueOf(data.length));
        }
        conn.setConnectTimeout(90*1000);
        conn.setReadTimeout(90*1000);
        OutputStream outStream = conn.getOutputStream();
        outStream.write(data);
        outStream.flush();
        outStream.close();
        if(conn.getResponseCode()==200){
            InputStream inStream = conn.getInputStream();
            result=new String(readStream(inStream));
        }
        return result;
    }

    /* 发送 post请求 用HTTPclient 发送请求*/
    public static ByteArrayInputStream sendPost(String URL, String json) {
        InputStream inputStream = null;
        ByteArrayInputStream byteArrayInputStream = null;
        // 创建默认的httpClient实例.
        CloseableHttpClient httpclient = HttpClients.createDefault();
        // 创建httppost
        HttpPost httppost = new HttpPost(URL);
        httppost.addHeader("Content-type", "application/json; charset=utf-8");
        httppost.setHeader("Accept", "application/json");
        try {
            StringEntity s = new StringEntity(json, Charset.forName("UTF-8"));
            s.setContentEncoding("UTF-8");
            httppost.setEntity(s);
            HttpResponse response = httpclient.execute(httppost);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
                // 获取相应实体
                HttpEntity entity = response.getEntity();
                inputStream = entity.getContent();
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                // 创建一个Buffer字符串
                byte[] buffer = new byte[1024];
                // 每次读取的字符串长度，如果为-1，代表全部读取完毕
                int len = 0;
                // 使用一个输入流从buffer里把数据读取出来
                while ((len = inputStream.read(buffer)) != -1) {
                    // 用输出流往buffer里写入数据，中间参数代表从哪个位置开始读，len代表读取的长度
                    outStream.write(buffer, 0, len);
                }
                // 关闭输入流
                inputStream.close();
                // 把outStream里的数据写入内存
                byteArrayInputStream = new ByteArrayInputStream(outStream.toByteArray());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭连接,释放资源
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return byteArrayInputStream;
    }

    public static String sendPost2(String url, String param, String encoding) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {

            String urlNameString = url + param;
            URL realUrl = new URL(urlNameString);
            // 打开和URL之间的连接
            logger.info("--sendPost--urlNameString = " + urlNameString);
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            logger.info("sendPost--out:" + out);

            // 发送请求参数
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), encoding));
            logger.info("sendPost--in:" + in);
            //   in = new BufferedReader(
            //         new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
            logger.info("result = [" + result + "]");
        } catch (Exception e) {
            logger.info("--sendPost--发送POST请求异常:", e);
            logger.error("sendPost2发送POST请求出现异常" + e.getMessage());
        }
        //使用finally块来关闭输出流、输入流
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                logger.info("--sendPost--发送POST请求异常:", ex);
                logger.error("发送 POST 请求出现异常" + ex.getMessage());
            }
        }
        return result;
    }

    public static String request(String path) throws  Exception {
        String result="";
        String encoding="UTF-8";
        String params ="";
        //System.out.println(path);
        //LOG.info("打印此处的字符串"+params);
        byte[] data = params.getBytes(encoding);
        URL url =new URL(path);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json; charset=" + encoding);
        conn.setRequestProperty("Content-Length", String.valueOf(data.length));
        conn.setConnectTimeout(90*1000);
        conn.setReadTimeout(90 * 1000);
        OutputStream outStream = conn.getOutputStream();
        outStream.write(data);
        outStream.flush();
        outStream.close();
        //System.out.println(conn.getResponseCode()); //响应代码 200表示成功
        if(conn.getResponseCode()==200){
            InputStream inStream = conn.getInputStream();
            result=readStream(inStream);
        }
        return result;
    }

    public static String request3(String path) throws  Exception {
        String result="";
        String encoding="UTF-8";
        String params ="";
        //System.out.println(path);
        //LOG.info("打印此处的字符串"+params);
        byte[] data = params.getBytes(encoding);
        URL url =new URL(path);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("GET");
        conn.setDoOutput(false);
        conn.setRequestProperty("X-TOKEN","VNMPradQNkGYjKXeqUe/GA==");
        conn.setConnectTimeout(90*1000);
        conn.setReadTimeout(90 * 1000);
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
       /* conn.disconnect();
        reader.close();*/
        //System.out.println(conn.getResponseCode()); //响应代码 200表示成功
        if(conn.getResponseCode()==200){
            InputStream inStream = conn.getInputStream();
            result=readStream(inStream);
        }
        return result;
    }


    public static String request2(String path,Object json) throws  Exception {
        String result="";
        String encoding="UTF-8";
        Gson gson = new Gson();
        String params = gson.toJson(json);
        byte[] data = params.getBytes(encoding);
        URL url =new URL(path);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();

        SSLSocketFactory oldSocketFactory = null;
        HostnameVerifier oldHostnameVerifier = null;

        boolean useHttps = path.startsWith("https");
        if (useHttps) {
            HttpsURLConnection https = (HttpsURLConnection) conn;
            oldSocketFactory = trustAllHosts(https);
            oldHostnameVerifier = https.getHostnameVerifier();
            https.setHostnameVerifier(DO_NOT_VERIFY);
        }

        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json; charset="+ encoding);
        conn.setRequestProperty("Content-Length", String.valueOf(data.length));
        conn.setConnectTimeout(90*1000);
        conn.setReadTimeout(90*1000);
        OutputStream outStream = conn.getOutputStream();
        outStream.write(data);
        outStream.flush();
        outStream.close();
        if(conn.getResponseCode()==200){
            InputStream inStream = conn.getInputStream();
            result=new String(readStream(inStream));

        }
        return result;
    }

    public static String sendPost(String url,Map<String,String> map,String encoding) throws IOException {
        String body = "";
        //创建httpclient对象
        CloseableHttpClient client = HttpClients.createDefault();
        try {
            RequestConfig config = RequestConfig.custom()
                    .setConnectionRequestTimeout(10800).setConnectTimeout(10800).setSocketTimeout(10800).build();
            //创建post方式请求对象
            HttpPost httpPost = new HttpPost(url);
            httpPost.setConfig(config);
            //装填参数
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            if(map!=null){
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                }
            }
            //设置参数到请求对象中
            httpPost.setEntity(new UrlEncodedFormEntity(nvps, encoding));

            //System.out.println("请求地址："+url);
            //System.out.println("请求参数："+nvps.toString());

            //设置header信息
            //指定报文头【Content-type】、【User-Agent】
            /*httpPost.setHeader("Content-type", "application/x-www-form-urlencoded");
            httpPost.setHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");*/

            //执行请求操作，并拿到结果（同步阻塞）
            CloseableHttpResponse response = client.execute(httpPost);
            //获取结果实体
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                //按指定编码转换结果实体为String类型
                body = EntityUtils.toString(entity, encoding);
            }
            EntityUtils.consume(entity);
            //释放链接
            response.close();
            client.close();
        }  catch (IOException e) {
            e.printStackTrace();
        } finally{
            try {
                if (client != null) {
                    client.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return body;
    }

    public static Boolean checkhealth(String urlstr) throws Exception {
        URL url = new URL(urlstr);
        HttpURLConnection huc = (HttpURLConnection) url.openConnection();
        huc.setRequestMethod("HEAD");
        int responseCode = huc.getResponseCode();
        return HttpURLConnection.HTTP_OK==responseCode;

    }


    public static String request(HttpURLConnection conn,String encoding){
        String result="";
         try {
            String params="";
            byte[] data =params.getBytes(encoding);
            OutputStream outStream = conn.getOutputStream();
            outStream.write(data);
            outStream.flush();
            outStream.close();
            if(conn.getResponseCode()==200){
                InputStream inStream = conn.getInputStream();
                result=new String(readStream(inStream));
            }
         }catch (Exception e){

         }
        return result;
    }

    /**
     * 读取�?
     *
     * @param inStream
     * @return 字节数组
     * @throws Exception
     */
    public static String readStream(InputStream inStream) throws Exception {
        //这里的编码规则要与上面的相对应
        BufferedReader br = new BufferedReader(new InputStreamReader(inStream,"UTF-8"));
        String tempbf;
        StringBuffer html = new StringBuffer(100);
        while ((tempbf = br.readLine()) != null) {
            html.append(tempbf +"\n");
        }
        inStream.close();
        return html.toString();
    }

    /**
     * 发起http请求并获取结果
     * @author speng
     * @param requestUrl 请求地址
     * @param requestMethod 请求方式（GET、POST）
     * @param outputStr 提交的数据
     * @return JSONObject(通过JSONObject.get(key)的方式获取json对象的属性值)
     */
    public static JSONObject httpRequest(String requestUrl, String requestMethod, String outputStr) {
        JSONObject jsonObject = null;
        StringBuffer buffer = new StringBuffer();
        InputStream inputStream = null;
        try {
            URL url = new URL(requestUrl);
            HttpURLConnection httpUrlConn = (HttpURLConnection) url.openConnection();
            httpUrlConn.setDoOutput(true);
            httpUrlConn.setDoInput(true);
            httpUrlConn.setUseCaches(false);
            httpUrlConn.setRequestProperty("Accept", "application/json"); // 设置接收数据的格式
            httpUrlConn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            // 设置请求方式（GET/POST）
            httpUrlConn.setRequestMethod(requestMethod);
            //创建connection连接
            httpUrlConn.connect();
            // 当有数据需要提交时
            if (null != outputStr) {
                OutputStream outputStream = httpUrlConn.getOutputStream();
                // 注意编码格式，防止中文乱码
                outputStream.write(outputStr.getBytes("UTF-8"));
                outputStream.close();
            }
            //将返回的输入流转换成字符串
            inputStream = httpUrlConn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String str = null;
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }
            bufferedReader.close();
            inputStreamReader.close();
            // 释放资源
            inputStream.close();
            inputStream = null;
            httpUrlConn.disconnect();
            jsonObject = JSONObject.parseObject(buffer.toString());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(inputStream!=null){
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return jsonObject;
    }

    public static JSONObject httpRequest2(String requestUrl, String requestMethod, String outputStr, String headerKey[], String headerValue[]) throws  Exception {
        JSONObject jsonObject = null;
        StringBuffer buffer = new StringBuffer();
        InputStream inputStream = null;
        try {
            URL url = new URL(requestUrl);
            HttpURLConnection httpUrlConn = (HttpURLConnection) url.openConnection();
            httpUrlConn.setDoOutput(true);
            httpUrlConn.setDoInput(true);
            httpUrlConn.setUseCaches(false);
            httpUrlConn.setRequestProperty("Accept", "application/json"); // 设置接收数据的格式
            httpUrlConn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            for(int i=0;i<headerKey.length;i++){
                httpUrlConn.setRequestProperty(headerKey[i],headerValue[i]);
            }
            // 设置请求方式（GET/POST）
            httpUrlConn.setRequestMethod(requestMethod);
            //创建connection连接
            httpUrlConn.connect();
            // 当有数据需要提交时
            if (null != outputStr) {
                OutputStream outputStream = httpUrlConn.getOutputStream();
                // 注意编码格式，防止中文乱码
                outputStream.write(outputStr.getBytes("UTF-8"));
                outputStream.close();
            }
            //将返回的输入流转换成字符串
            inputStream = httpUrlConn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String str = null;
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }
            bufferedReader.close();
            inputStreamReader.close();
            // 释放资源
            inputStream.close();
            inputStream = null;
            httpUrlConn.disconnect();
            jsonObject = JSONObject.parseObject(buffer.toString());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(inputStream!=null){
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return jsonObject;
    }

    /**
     * 发起http请求并获取结果
     * @author zmn
     * @param requestUrl 请求地址
     * @param requestMethod 请求方式（GET、POST）
     * @param outputStr 提交的数据
     * @return
     */
    public static String getStringByHttpRequest(String requestUrl, String requestMethod, String outputStr) {
        String resStr = "";
        StringBuffer buffer = new StringBuffer();
        InputStream inputStream = null;
        try {
            URL url = new URL(requestUrl);
            HttpURLConnection httpUrlConn = (HttpURLConnection) url.openConnection();
            httpUrlConn.setDoOutput(true);
            httpUrlConn.setDoInput(true);
            httpUrlConn.setUseCaches(false);
            httpUrlConn.setRequestProperty("Accept", "application/json"); // 设置接收数据的格式
            httpUrlConn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            // 设置请求方式（GET/POST）
            httpUrlConn.setRequestMethod(requestMethod);
            //创建connection连接
            httpUrlConn.connect();
            // 当有数据需要提交时
            if (null != outputStr) {
                OutputStream outputStream = httpUrlConn.getOutputStream();
                // 注意编码格式，防止中文乱码
                outputStream.write(outputStr.getBytes("UTF-8"));
                outputStream.close();
            }
            //将返回的输入流转换成字符串
            inputStream = httpUrlConn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String str = null;
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }
            bufferedReader.close();
            inputStreamReader.close();
            // 释放资源
            inputStream.close();
            inputStream = null;
            httpUrlConn.disconnect();
            resStr=buffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(inputStream!=null){
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return resStr;
    }

    /**
     * 模拟请求
     *
     * @param url       资源地址
     * @param map   参数列表
     * @param encoding  编码
     * @return http://blog.csdn.net/zhousenshan/article/details/51245042
     * @throws ParseException
     * @throws IOException
     */
    public static String dopsot(String url, HashMap<String, Object> map, String encoding) throws ParseException, IOException{
        String body = "";
        //创建httpclient对象
        CloseableHttpClient client = HttpClients.createDefault();
        //创建post方式请求对象
        HttpPost httpPost = new HttpPost(url);
        //2分钟超时
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(120000).setConnectionRequestTimeout(120000)
                .setSocketTimeout(120000).build();
        httpPost.setConfig(requestConfig);
        //装填参数
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        if(map!=null){
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
            }
        }
        try {
            //设置参数到请求对象中
            httpPost.setEntity(new UrlEncodedFormEntity(nvps, encoding));
            //System.out.println("请求地址：" + url);
            //System.out.println("请求参数：" + nvps.toString());
            //设置header信息
            //指定报文头【Content-type】、【User-Agent】
            httpPost.setHeader("Content-type", "application/x-www-form-urlencoded");
            httpPost.setHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            //执行请求操作，并拿到结果（同步阻塞）
            CloseableHttpResponse response = client.execute(httpPost);
            try{
                //获取结果实体
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    //按指定编码转换结果实体为String类型
                    body = EntityUtils.toString(entity, encoding);
                }
            }catch (Exception e) {
                e.printStackTrace();
            }finally {
                response.close();
            }
        }catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭连接,释放资源
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return body;
    }

    /**
     * psot请求 (设置请求头)
     *
     * @param url        资源地址
     * @param jsonObject 参数列表
     * @param encoding   编码
     * @throws ParseException
     * @throws IOException
     */
    public static String dopsot(String url, JSONObject jsonObject, String encoding,Map<String, String> header) throws ParseException, IOException{
        String body = "";
        //创建httpclient对象
        CloseableHttpClient client = HttpClients.createDefault();
        //创建post方式请求对象
        HttpPost httpPost = new HttpPost(url);
        //2分钟超时
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(120000).setConnectionRequestTimeout(120000)
                .setSocketTimeout(120000).build();
        httpPost.setConfig(requestConfig);

        try {
            //设置参数到请求对象中
            StringEntity stringEntity = new StringEntity(jsonObject.toString());
            stringEntity.setContentEncoding(encoding);
            //发送json数据需要设置contentType
            stringEntity.setContentType("application/json");
            httpPost.setEntity(stringEntity);
            //设置header信息
            //指定报文头【Content-type】、【User-Agent】
            httpPost.setHeader("Content-type", "application/x-www-form-urlencoded");
            httpPost.setHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            // 设置通用的请求属性
            if (header != null) {
                for (Map.Entry<String, String> entry : header.entrySet()) {
                    httpPost.setHeader(entry.getKey(), entry.getValue());
                }
            }
            //执行请求操作，并拿到结果（同步阻塞）
            CloseableHttpResponse response = client.execute(httpPost);
            try{
                //获取结果实体
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    //按指定编码转换结果实体为String类型
                    body = EntityUtils.toString(entity, encoding);
                }
            }catch (Exception e) {
                e.printStackTrace();
            }finally {
                response.close();
            }
        }catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭连接,释放资源
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return body;
    }

    public static String request5(String path, String param,Map<String, String> header) throws  Exception {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        URL realUrl = new URL(path);
        // 打开和URL之间的连接
        URLConnection conn = realUrl.openConnection();
        //设置超时时间
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(30000);
        // 设置通用的请求属性
        if (header != null) {
            for (Map.Entry<String, String> entry : header.entrySet()) {
                conn.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
        conn.setRequestProperty("accept", "*/*");
        conn.setRequestProperty("connection", "Keep-Alive");
        conn.setRequestProperty("user-agent",
                "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
        // 发送POST请求必须设置如下两行
        conn.setDoOutput(true);
        conn.setDoInput(true);
        // 获取URLConnection对象对应的输出流
        out = new PrintWriter(conn.getOutputStream());
        // 发送请求参数
        out.print(param);
        // flush输出流的缓冲
        out.flush();
        // 定义BufferedReader输入流来读取URL的响应
        in = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), "utf8"));
        String line;
        while ((line = in.readLine()) != null) {
            result += line;
        }
        if (out != null) {
            out.close();
        }
        if (in != null) {
            in.close();
        }
        return result;
    }

    private static final TrustManager[] trustAllCerts=new TrustManager[]{new X509ExtendedTrustManager() {
        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s, Socket socket) throws CertificateException {

        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s, Socket socket) throws CertificateException {

        }

        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine) throws CertificateException {

        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine) throws CertificateException {

        }

        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }};

    /**
     * 设置不验证主机
     */
    private static final HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    /**
     * 信任所有
     * @param connection
     * @return
     */
    private static SSLSocketFactory trustAllHosts(HttpsURLConnection connection) {
        SSLSocketFactory oldFactory = connection.getSSLSocketFactory();
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            SSLSocketFactory newFactory = sc.getSocketFactory();
            connection.setSSLSocketFactory(newFactory);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return oldFactory;
    }

    public static void main(String[] args) throws Exception{
        String path = HttpClient.request("http://gdwsc.nsenz.com/api/testindex1?pageid=a29dff6a-ff71-41db-a9c5-4f4e65b5a1ff&channel=2&oucode=100002");
        System.out.println(path);
    }
}
