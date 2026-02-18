package com.sunbox.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.dom4j.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.*;

/**
 * Created by ny on 2019/5/17.
 */
public class AliJsonUtil {

     /* String 转 org.dom4j.Document
     * @param xml
     * @return
             * @throws DocumentException
     */
    public static Document strToDocument(String xml){
        try {
            //加上xml标签是为了获取最外层的标签，如果不需要可以去掉
            if(xml.indexOf("<xml>")==0){
                return DocumentHelper.parseText("<xml>"+xml+"</xml>");
            }else{
                return DocumentHelper.parseText(xml);
            }

        } catch (DocumentException e) {
            return null;
        }
    }

    /**
     * org.dom4j.Document 转  com.alibaba.fastjson.JSONObject
     * @param xml
     * @return
     * @throws DocumentException
     */
    public static JSONObject documentToJSONObject(String xml){
        return elementToJSONObject(strToDocument(xml).getRootElement());
    }

    /**
     * org.dom4j.Element 转  com.alibaba.fastjson.JSONObject
     * @param node
     * @return
     */
    public static JSONObject elementToJSONObject(Element node) {
        JSONObject result = new JSONObject();
        // 当前节点的名称、文本内容和属性
        List<Attribute> listAttr = node.attributes();// 当前节点的所有属性的list
        for (Attribute attr : listAttr) {// 遍历当前节点的所有属性
            result.put(attr.getName(), attr.getValue());
        }
        // 递归遍历当前节点所有的子节点
        List<Element> listElement = node.elements();// 所有一级子节点的list
        if (!listElement.isEmpty()) {
            for (Element e : listElement) {// 遍历所有一级子节点
                if (e.attributes().isEmpty() && e.elements().isEmpty()) // 判断一级节点是否有属性和子节点
                    result.put(e.getName(), e.getTextTrim());// 沒有则将当前节点作为上级节点的属性对待
                else {
                    if (!result.containsKey(e.getName())) // 判断父节点是否存在该一级节点名称的属性
                        result.put(e.getName(), new JSONArray());// 没有则创建
                    ((JSONArray) result.get(e.getName())).add(elementToJSONObject(e));// 将该一级节点放入该节点名称的属性对应的值中
                }
            }
        }
        return result;
    }
    /**
     * 将Map转换为XML格式的字符串
     * @param data Map类型数据
     * @return XML格式的字符串
     * @throws Exception
    */
    public static String mapToXml(Map<String, String> data){
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder= documentBuilderFactory.newDocumentBuilder();
            org.w3c.dom.Document document = documentBuilder.newDocument();
            org.w3c.dom.Element root = document.createElement("body");
            document.appendChild(root);
            for (String key: data.keySet()) {
                String value = data.get(key);
                if (value == null) {
                    value = "";
                }
                value = value.trim();
                org.w3c.dom.Element filed = document.createElement(key);
                filed.appendChild(document.createTextNode(value));
                root.appendChild(filed);
            }
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            DOMSource source = new DOMSource(document);
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            document.setXmlStandalone(true);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            transformer.transform(source, result);
            String output = writer.getBuffer().toString(); //.replaceAll("\n|\r", "");
            writer.close();
            return output;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static void main(String args[])throws Exception{
      /* Calendar beforeTime = Calendar.getInstance();
        beforeTime.add(Calendar.MINUTE, -5);
        Date before = beforeTime.getTime();
        String beforeStr= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(before);
        String nowStr= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        Map<String,String> map = new HashMap<>();
        map.put("order_ids","12345,45678");*//*
        map.put("create_end_time",nowStr);
        map.put("modify_time_from"," ");
        map.put("modify_time_to"," ");
        map.put("order_status","02");//02待发货订单
        map.put("merchant_id"," ");*/
      /* System.out.println(mapToXml(map));*/

        /*ICBCECouponVO iCBCECouponVO = new ICBCECouponVO();

        iCBCECouponVO.setCreate_start_time(beforeStr);
        iCBCECouponVO.setCreate_end_time(nowStr);
        iCBCECouponVO.setOrder_status("02");
        iCBCECouponVO.setModify_time_from("");
        iCBCECouponVO.setModify_time_to("");
        iCBCECouponVO.setMerchant_id("");
        String xmlResult = beanToXml(iCBCECouponVO, ICBCECouponVO.class);
        System.out.println(xmlResult);*/

    /*    String resultStr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<response>\n" +
                "<head>\n" +
                "<method>icbcb2c.order.list</method>\n" +
                "<req_sid>20130508000000001</req_sid>\n" +
                "<version>1.0</version>\n" +
                "<timestamp>123111111111</timestamp>\n" +
                "<app_key>10100011</app_key>\n" +
                "<auth_code>AfcdE</auth_code>\n" +
                "<ret_code>00000000</ret_code>\n" +
                "<ret_msg>OK</ret_msg>\n" +
                "<sign>581149147A@#%^ASDFQEQW</sign>\n" +
                "</head>\n" +
                "<body>\n" +
                "<order_list>\n" +
                "<order>\n" +
                "<order_id>12312312312</order_id>\n" +
                "<order_create_time>2013-12-12 10:00:00</order_create_time>\n" +
                "<order_modify_time>2013-12-12 10:00:00</order_modify_time>\n" +
                "<order_status>01</order_status>\n" +
                "</order>\n" +
                "<order>\n" +
                "<order_id>12312312315</order_id>\n" +
                "<order_create_time>2013-12-22 10:00:00</order_create_time>\n" +
                "<order_modify_time>2013-12-23 10:00:00</order_modify_time>\n" +
                "<order_status>02</order_status>\n" +
                "</order>\n" +
                "......\n" +
                "</order_list>\n" +
                "</body>\n" +
                "</response>\n";
        JSONObject resultObj = AliJsonUtil.documentToJSONObject(resultStr);
        System.out.println(resultObj.toJSONString());
        JSONArray headJsonArray = (JSONArray)resultObj.get("body");
        if(headJsonArray != null){
            JSONObject bodyobject = (JSONObject)headJsonArray.get(0);
            JSONArray orderlistArray = (JSONArray)bodyobject.get("order_list");
            if(orderlistArray != null){
                JSONObject orderlistObject = (JSONObject)orderlistArray.get(0);
                JSONArray orderArray = (JSONArray)orderlistObject.get("order");
                if(orderArray != null){
                    for (Object order:orderArray) {
                        JSONObject jsonObject = JSON.parseObject(order.toString());
                        String order_id = (String)jsonObject.get("order_id");
                        System.out.println(order_id);
                    }
                }

            }
        }*/

    String resultStr="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<response>\n" +
            "<head>\n" +
            "<method>icbcb2c.order.detail</method>\n" +
            "<req_sid>20130508000000001</req_sid>\n" +
            "<version>1.0</version>\n" +
            "<timestamp>123111111111</timestamp>\n" +
            "<app_key>10100011</app_key>\n" +
            "<auth_code>AfcdE</auth_code>\n" +
            "<ret_code>00000000</ret_code>\n" +
            "<ret_msg>OK</ret_msg>\n" +
            "<sign>581149147A@#%^ASDFQEQW</sign>\n" +
            "</head>\n" +
            "<body>\n" +
            "<order_list>\n" +
            "    <order>\n" +
            "\t\t<order_id>12312312312</order_id>\n" +
            "  \t\t<order_modify_time>2013-12-12 10:00:00</order_modify_time>\n" +
            "<order_status>01</order_status>\n" +
            "   \t\t<order_buyer_remark></order_buyer_remark>\n" +
            "<order_seller_remark></order_seller_remark>\n" +
            "<merchant2member_remark></merchant2member_remark>\n" +
            "<order_buyer_id>EP2014062400008001</order_buyer_id>\n" +
            "<order_buyer_username>XXX</order_buyer_username>\n" +
            "<order_buyer_name> </order_buyer_name>\n" +
            "<order_create_time>2014-08-02 11:04:45</order_create_time>\n" +
            "<order_amount>6</order_amount>\n" +
            "<order_credit_amount>0</order_credit_amount>\n" +
            "<credit_liquid_amount>0</credit_liquid_amount>\n" +
            "<order_other_discount>0</order_other_discount>\n" +
            "<order_channel>1</order_channel>\n" +
            "<order_coupon_amount>0</order_coupon_amount>\n" +
            "<order_ewelfare_discount>0</order_ewelfare_discount>\n" +
            "<order_ins_num></order_ins_num>\n" +
            "<ins_fee_mer></ins_fee_mer>\n" +
            "<order_flag_color></order_flag_color>\n" +
            "<discounts>\n" +
            "<discount>\n" +
            "<discount_type>1</discount_type>\n" +
            "<discount_amount>23</discount_amount>\n" +
            "</discount>\n" +
            "<discount>\n" +
            "<discount_type>2</discount_type>\n" +
            "<discount_amount>45</discount_amount>\n" +
            "</discount>\n" +
            "</discounts>\n" +
            "\t\t<products>\n" +
            "\t\t\t<product>\n" +
            "              <product_id>899001</product_id>\n" +
            "              <product_sku_id>10001</product_sku_id>\n" +
            "              <product_code>77001</product_code>\n" +
            "              <product_name>某冰箱</product_name>\n" +
            "              <product_number>123</product_number>\n" +
            "              <product_price>商品价格</product_price>\n" +
            "              <product_discount>商品优惠金额</product_discount>\n" +
            "<product_prop_info>日期：2019-02-25</product_prop_info>\n" +
            "              <refund_process></refund_process>\n" +
            "              <refund_num></refund_num>\n" +
            "              <activities>\n" +
            "\t\t\t\t\t<activity>\n" +
            "<activity_id>201305150000001</activity_id>\n" +
            "<activity_type>groupbuy</activity_type>\n" +
            "<activity_name>双十促销</activity_name>\n" +
            "</activity>\n" +
            "\t\t\t\t</activities>\n" +
            "              <tringproducts>\n" +
            "\t\t\t\t\t<tringproduct>\n" +
            "              <product_id>899001</product_id>\n" +
            "              <product_sku_id>10001</product_sku_id>\n" +
            "              <product_code>77001</product_code>\n" +
            "              <product_name>某冰箱</product_name>\n" +
            "              <product_number>123</product_number>\n" +
            "              <product_price>商品搭售价格</product_price>\n" +
            "<product_prop_info>日期：2019-02-25</product_prop_info>\n" +
            "              <refund_process></refund_process>\n" +
            "              <refund_num></refund_num>\n" +
            "</tringproduct>\n" +
            "\t\t\t\t</tringproducts>\n" +
            "              <giftproducts>\n" +
            "\t\t\t\t\t<giftproduct>\n" +
            "              <product_id>899001</product_id>\n" +
            "              <product_sku_id></product_sku_id>\n" +
            "              <product_code>77001</product_code>\n" +
            "              <product_name>某冰箱</product_name>\n" +
            "              <product_number>123</product_number>\n" +
            "</giftproduct>\n" +
            "\t\t\t\t</giftproducts>\n" +
            "   \t\t\t</product>\n" +
            "</products>\n" +
            "  \t\t\t <invoice>\n" +
            "\t\t<invoice_type>01</invoice_type>\n" +
            "\t\t<invoice_title>个人自用</invoice_title>\n" +
            "\t\t<invoice_content>发票内容</invoice_content>\n" +
            "        <register_address></register_address>\n" +
            "        <register_tel></register_tel>\n" +
            "        <register_bank></register_bank>\n" +
            "        <register_account></register_account>\n" +
            "        <taxpayer_id></taxpayer_id>\n" +
            "\t\t\t</invoice>\n" +
            "\t\t\t\t<payment>\n" +
            "\t\t\t\t\t<order_pay_time>12315426</order_pay_time>\n" +
            "                    <order_pay_amount>900.00</order_pay_amount>\n" +
            " \t\t\t\t\t<order_cash_amount>900.00</order_cash_amount>\n" +
            "                        <order_pay_sys>ICBC</order_pay_sys>\n" +
            "                    <order_discount_amount>100.00</order_discount_amount>\n" +
            "                        <order_freight>32.00</order_freight>\n" +
            "\t\t\t\t\t<pay_serial >5671823123121</pay_serial>\n" +
            "         <coupons>\n" +
            "\t\t\t    <coupon>\n" +
            "                                    <coupon_id>20140001</coupon_id>\n" +
            "                                    <coupon_promo_id>00000212</coupon_promo_id>\n" +
            "                                    <coupon_promo_name>活动名称</coupon_promo_name>\n" +
            "                                    <coupon_org_amount>100.00</coupon_org_amount>\n" +
            "                                    <coupon_use_amount>65.00</coupon_use_amount>\n" +
            "                                    <coupon_type>01</coupon_type>\n" +
            "                                </coupon>\n" +
            "\t\t\t</coupons>\n" +
            "\t\t\t\t\t</payment>\n" +
            "\t\t\t\t<consignee>\n" +
            "\t\t\t\t<consignee_name>张三</consignee_name>\n" +
            "\t\t\t\t<consignee_province>山东省</consignee_province>\n" +
            "\t\t\t\t<consignee_province_id>100000</consignee_province_id>\n" +
            "\t\t\t\t<consignee_city>青岛市</consignee_city>\n" +
            "<consignee_city_id>100100</consignee_city_id>\n" +
            "<consignee_district>崂山区</consignee_district>\n" +
            "<consignee_district_id>100199</consignee_district_id>\n" +
            "<consignee_address>海尔路19号北村小区32号网点中国邮政速递物流有限公司</consignee_address>\n" +
            "<consignee_zipcode>100080</consignee_zipcode>\n" +
            "<consignee_total_address>山东省青岛市崂山区海尔路19号北村小区32号网点中国邮政速递物流有限公司(100080)</consignee_total_address> \n" +
            "<consignee_mobile>139123123123</consignee_mobile>\n" +
            "<consignee_phone>010-123123123</consignee_phone>\n" +
            "<consignee_time>1</consignee_time>\n" +
            "<consignee_idcardnum>1</consignee_idcardnum>\n" +
            "<consignee_email>1</consignee_email>\n" +
            "<merDefined1> QQ号:1234567</merDefined1>\n" +
            "<merDefined2> QQ号:1234567</merDefined2>\n" +
            "<merDefined3> QQ号:1234567</merDefined3>\n" +
            "</consignee>\n" +
            "\t\t\t</order>\n" +
            "</order_list>\n" +
            "</body>\n" +
            "</response>\n";
        JSONObject resultObj = AliJsonUtil.documentToJSONObject(resultStr);
        JSONArray headJsonArray = (JSONArray)resultObj.get("body");

        JSONObject bodyobject = (JSONObject)headJsonArray.get(0);
        JSONArray orderlistArray = (JSONArray)bodyobject.get("order_list");

        JSONObject orderlistObject = (JSONObject)orderlistArray.get(0);
        JSONArray orderArray = (JSONArray)orderlistObject.get("order");
        JSONObject orderObject = (JSONObject)orderArray.get(0);
        //订单总金额
        //String order_amount = (String)orderObject.get("order_amount");
        JSONArray productsArray = (JSONArray)orderObject.get("products");
        JSONObject  productsObject = (JSONObject)productsArray.get(0);
        JSONArray productArray = (JSONArray)productsObject.get("product");
       // List<ThirdCouponProduct> list = new ArrayList();
        for (Object object:productArray) {
            JSONObject jsonObject = JSON.parseObject(object.toString());
            String product_id = (String)jsonObject.get("product_id");
            String product_sku_id = (String)jsonObject.get("product_sku_id");
            String product_code = (String)jsonObject.get("product_code");//商品商户编码
            String product_name = (String)jsonObject.get("product_name");
            String product_number = (String)jsonObject.get("product_number");
            String product_price = (String)jsonObject.get("product_price");



        }





String resultStr1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<response>\n" +
        "<head>\n" +
        "<method>icbcb2c.order.sendmess</method>\n" +
        "<req_sid>20130508000000001</req_sid>\n" +
        "<version>1.0</version>\n" +
        "<timestamp>123111111111</timestamp>\n" +
        "<app_key>10100011</app_key>\n" +
        "<auth_code>AfcdE</auth_code>\n" +
        "<ret_code>00000000</ret_code>\n" +
        "<ret_msg>OK</ret_msg>\n" +
        "<sign>581149147A@#%^ASDFQEQW</sign>\n" +
        "</head>\n" +
        "</response>\n";
        JSONObject resultObj1 = AliJsonUtil.documentToJSONObject(resultStr1);
        System.out.println(resultObj1.toJSONString());
    }


    /**
     * 将对象转化成xml（GBK编码）
     *
     * @param obj
     * @param load
     * @return
     * @throws JAXBException
     */
    public static String beanToXml(Object obj, java.lang.Class<?> load)
            throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(load);
        Marshaller marshaller = context.createMarshaller();
        //格式化输出xml
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        //设置输出xml编码格式
        marshaller.setProperty(Marshaller.JAXB_ENCODING,"UTF-8");
        StringWriter writer = new StringWriter();
        marshaller.marshal(obj, writer);
        //去掉standalone属性
        return writer.toString().replace("standalone=\"yes\"", "");
    }

}
