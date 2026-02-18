package com.sunbox.util;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by xcd on 2016/2/17.
 */
public class WebServerUtil {

    public static String getXmlInfo(Map map) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<req>");
        Iterator iter = map.entrySet().iterator();
        while (iter.hasNext()){
            Map.Entry entry = (Map.Entry)iter.next();
            sb.append("<"+entry.getKey()+">"+entry.getValue()+"</"+entry.getKey()+">");
        }
        sb.append("</req>");
        return sb.toString();
    }
}
