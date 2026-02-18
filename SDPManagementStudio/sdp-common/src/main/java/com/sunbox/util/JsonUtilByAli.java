package com.sunbox.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.dom4j.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 此引用了com.alibaba.fastjson.JSONArray和JSONObject，解决了解析数据精度丢失的问题
 * 如果引用net.sf.json.JSONArr和JSONObject(目前调用的是此架包json-lib-2.4-jdk15.jar)，如2323433.34会变成2323433.2，5020650.77会变成5020651
 */
public class JsonUtilByAli {

    public static JSONObject parse1(String protocolXML) {

        Document doc= null;
        JSONObject jsonObject = null;
        try {
            doc = DocumentHelper.parseText(protocolXML);
            Element books = doc.getRootElement();
            jsonObject = (JSONObject)xml2jsonObject(books, false);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private static Object xml2jsonObject(Element element,boolean flag) {
        JSONObject jsonObject = new JSONObject();

        List<Element> elements = element.elements();
        if (elements.size() == 0) {
            jsonObject.put(element.getName(),element.getText());
            if (!element.isRootElement()) {
                return element.getText();
            }
        } else if (elements.size() == 1) {
            jsonObject.put(elements.get(0).getName(), xml2jsonObject(elements.get(0),false));
        } else if (elements.size() > 1) {
            // 多个子节点的话就得考虑list的情况了，比如多个子节点有节点名称相同的
            // 构造一个map用来去重

            Map<String, Element> tempMap = new HashMap<String, Element>();
            for (Element ele : elements) {
                tempMap.put(ele.getName(), ele);
            }
            Set<String> keySet = tempMap.keySet();
            for (String string : keySet) {
                Namespace namespace = tempMap.get(string).getNamespace();
                List<Element> elements2 = element.elements(new QName(string, namespace));
                // 如果同名的数目大于1则表示要构建list
                if (elements2.size() > 1) {
                    JSONArray jsonArray = new JSONArray();
                    for (Element ele : elements2) {
                        List<Element> elements1 = ele.elements();
                        JSONObject jsonObject1 = new JSONObject();
                        if (elements1.size()>1){
                            for(Element e : elements1){
                                if (null != e.attribute("name")){
                                    if (e.attributes().size()>1){
                                        JSONObject jsonObject2 = new JSONObject();
                                        List<Attribute> attributes = e.attributes();
                                        for(Attribute attr : attributes){
                                            jsonObject2.put(attr.getName(),attr.getValue());
                                        }
                                        jsonObject1.put(e.getName(),jsonObject2);
                                    }else {
                                        jsonObject1.put(e.attributeValue("name"),xml2jsonObject(e,true));
                                    }
                                }else {
                                    jsonObject1.put(ele.getName(),ele.attributeValue("name"));
                                    jsonObject1.put(e.getName(),xml2jsonObject(e,true));
                                }
                            }
                        }else {

                            List<Attribute> attributes = ele.attributes();
                            if (attributes.size()>1){
                                for(Attribute attr : attributes){
                                    jsonObject1.put(attr.getName(),attr.getValue());
                                }
                            }else if(attributes.size() == 1){
                                jsonObject1.put(ele.attributeValue("name"),xml2jsonObject(ele,false));
                            }else {
                                jsonObject1.put(ele.getName(),xml2jsonObject(ele,false));
                            }
                        }
                        jsonArray.add(jsonObject1);
                    }
                    if (flag){
                        return jsonArray;
                    }
                    jsonObject.put(string,jsonArray);
                } else {
                    // 同名的数量不大于1则直接递归去
                    if (null != elements2.get(0).attribute("name")){
                        jsonObject.put(elements2.get(0).attributeValue("name"), xml2jsonObject(elements2.get(0),false));
                    }else {
                        jsonObject.put(string, xml2jsonObject(elements2.get(0),false));
                    }
                }
            }
        }
        return jsonObject;
    }
}
