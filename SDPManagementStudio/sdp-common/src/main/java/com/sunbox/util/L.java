package com.sunbox.util;

import cn.hutool.core.convert.Convert;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 用于构建梳一固定格式的日志内容<br/>
 * <p>示例日志: 用户登录失败 (1234)[userId="1234",userName="user1"]</p><br/>
 * <div>
 * 示例代码: <br/>
 * <code>
 * L msg = L.b("用户登录失败")<br/>
 *     .bizId("1234")<br/>
 *     .p("userId", "1234")<br/>
 *     .p("userName", "user1");<br/><br/>
 * logger.info(msg);<br/>
 * </code>
 * </div>
 * @date 2023/6/7
 */
public class L {
    /** 日志信息内容 */
    private String message;

    /** 业务ID */
    private String bizId;

    private List<Object[]> log = new ArrayList<>(16);

    private L(String msg) {
        this.message = msg;
    }

    /**
     * 构建一个L对象
     * @return
     */
    public static L b(String msg) {
        return new L(msg);
    }

    public static L b() {
        return new L("");
    }

    /**
     * 增加一个K-V参数对儿
     * @param key 参数名
     * @param val 参数值
     * @return
     */
    public L p(String key, Object val) {
        Object[] pair = new Object[]{key, val};
        log.add(pair);
        return this;
    }

    /**
     * 增加一个K-V参数对儿
     * @param key
     * @param val
     * @return
     */
    public L pair(String key, Object val) {
        return p(key, val);
    }

    /**
     * 设置一个业务标识，此字段不是必填字段。
     * @param bizId
     * @return
     */
    public L bizId(String bizId) {
        this.bizId = bizId;
        return this;
    }

    /**
     * toString()的简化版
     * @return 字符串
     */
    public String s() {
        StringBuilder logStr = new StringBuilder();
        // message
        logStr.append(message).append(" ");
        // bizId
        if (Objects.nonNull(bizId)) {
            logStr.append("(").append(bizId).append(")");
        }
        // 业务参数
        if (log.size() > 0) {
            logStr.append("[");
            boolean needComma = false;
            for (Object[] o : log) {
                if (needComma) {
                    logStr.append(", ");
                }
                logStr.append(o[0])
                        .append("=\"")
                        .append(Convert.toStr(o[1]))
                        .append("\"");
                needComma = true;
            }
            logStr.append("]");
        }
        return logStr.toString();
    }

    @Override
    public String toString() {
        return s();
    }
}
