package sunbox.sdp.ambari.client;

import java.util.Map;
import java.util.List;
import java.io.*;
import java.net.*;
import java.util.Set;

/**
 * 通用http发送方法
 *
 * @author zhangchao
 */
public class HttpUtil
{
    public static StrResponse request(final String url, final String method,
                                                      final byte[] requestBody,final Map<String, String> headerMap, String responseEncoding) {
        BufferedReader in = null;
        BufferedReader errorReader = null;
        HttpURLConnection connection = null;
        StrResponse strResponse = null;
        try {
            StringBuilder result = new StringBuilder();
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            connection = (HttpURLConnection) realUrl.openConnection();
            connection.setRequestMethod(method);
            // 请求内容的长度
            if (null != requestBody && requestBody.length > 0) {
                connection.setRequestProperty("Content-Length", String.valueOf(requestBody.length));
            }
            // 自定义请求头
            if (null != headerMap && false == headerMap.isEmpty()) {
                Set<String> keySet = headerMap.keySet();
                for (String key : keySet) {
                    connection.setRequestProperty(key, headerMap.get(key));
                }
            }
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            // 把JSON作为字节流写入post请求的body中
            connection.setDoOutput(true);
            if (null != requestBody && requestBody.length > 0) {
                connection.getOutputStream().write(requestBody);
            }
            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream(), responseEncoding));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line).append("\n");
            }
            strResponse = new StrResponse();
            strResponse.setCode(connection.getResponseCode());
            // 返回的header
            Map<String, List<String>> map = connection.getHeaderFields();
            strResponse.setHeaders(map);
            // 返回的body
            String responseBody = result.toString();
            strResponse.setBody(responseBody);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (null != connection) {
                    StringBuilder result = new StringBuilder();
                    // 定义 BufferedReader输入流来读取URL的响应
                    errorReader = new BufferedReader(new InputStreamReader(
                            connection.getErrorStream(), responseEncoding));
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        result.append(line).append("\n");
                    }
                    strResponse = new StrResponse();
                    strResponse.setCode(connection.getResponseCode());
                    // 返回的header
                    Map<String, List<String>> map = connection.getHeaderFields();
                    strResponse.setHeaders(map);
                    // 返回的body
                    String responseBody = result.toString();
                    strResponse.setBody(responseBody);
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        } finally {
            try {
                if (null != in) {
                    in.close();
                }
                if (null != errorReader) {
                    errorReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return strResponse;
    }

    public static StrResponse request(final String url, final String method, final String requestBody,
                                                     final Map<String, String> headerMap, final String encoding) {
        // 字符串转成字节流
        byte[] bodyBytes = null;
        try {
            if (requestBody != null) {
                bodyBytes = requestBody.getBytes(encoding);
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return request(url, method, bodyBytes, headerMap, encoding);
    }

    public static StrResponse get(final String url, final String requestBody, final Map<String, String> headerMap) {
        return request(url, "GET", requestBody, headerMap, "UTF-8");
    }
}
