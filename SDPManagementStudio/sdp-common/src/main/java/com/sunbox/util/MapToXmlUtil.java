package com.sunbox.util;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by An Wentong on 2016/3/24 0024.
 */
public class MapToXmlUtil {

    public static String map2xml_Request(Map map){
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<Request>");
        Iterator iter = map.entrySet().iterator();
        while (iter.hasNext()){
            Map.Entry entry = (Map.Entry)iter.next();
            sb.append("<"+entry.getKey()+">"+entry.getValue()+"</"+entry.getKey()+">");
        }
        sb.append("</Request>");
        return sb.toString();
    }

    public static String map2xml_Response(Map map){
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<Response>");
        Iterator iter = map.entrySet().iterator();
        while (iter.hasNext()){
            Map.Entry entry = (Map.Entry)iter.next();
            sb.append("<"+entry.getKey()+">"+entry.getValue()+"</"+entry.getKey()+">");
        }
        sb.append("</Response>");
        return sb.toString();
    }
    public static String map2xml(Map map){
        StringBuilder sb = new StringBuilder();

        Iterator iter = map.entrySet().iterator();
        while (iter.hasNext()){
            Map.Entry entry = (Map.Entry)iter.next();
            sb.append("<"+entry.getKey()+">"+entry.getValue()+"</"+entry.getKey()+">");
        }

        return sb.toString();
    }

    public static String map2xml_req(Map map){
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

    public static String mapsign_req(Map map,String md5key){
        StringBuilder sb = new StringBuilder();
        Iterator iter = map.entrySet().iterator();
        while (iter.hasNext()){
            Map.Entry entry = (Map.Entry)iter.next();
            sb.append(entry.getKey()+"="+entry.getValue()+"&");
        }
        sb.append("Key="+md5key);
        return EncryptionUtil.getHash(sb.toString(),"md5");

    }
}
