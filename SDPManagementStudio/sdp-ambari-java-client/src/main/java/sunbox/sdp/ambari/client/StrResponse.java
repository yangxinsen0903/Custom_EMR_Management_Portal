package sunbox.sdp.ambari.client;


import java.util.List;
import java.util.Map;

/**
 * 接收HTTP返回数据的对象
 * @author zhangchao
 */
public class StrResponse {
    private int code = 200;
    private Map<String, List<String>> headers = null;
    private String body = null;

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getHeaderStr (String key) {
        List<String> list = this.headers.get(key);
        StringBuilder sb = new StringBuilder();
        for (String str : list) {
            sb.append(str);
        }
        return sb.toString();
    }

    public String getAllHeaderStr() {
        if (null == headers || headers.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String key : headers.keySet()) {
            List<String> list = headers.get(key);
            sb.append(key + ":\n");
            for (String str : list) {
                sb.append("    " + str + "\n");
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("StrResponse{");
        sb.append("code=").append(code);
        sb.append(", headers=").append(headers);
        sb.append(", body='").append(body).append('\'');
        sb.append('}');
        return sb.toString();
    }
}

