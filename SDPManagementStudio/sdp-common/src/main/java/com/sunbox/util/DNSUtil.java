package com.sunbox.util;

import cn.hutool.core.thread.ThreadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.Address;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * @author : [niyang]
 * @className : DNSUtil
 * @description : [描述说明该类的功能]
 * @createTime : [2023/5/29 6:15 PM]
 */
public class DNSUtil {

    private static final Logger logger = LoggerFactory.getLogger(DNSUtil.class);

    /**
     * 通过dns解析域名得到IP列表
     * @param host 域名
     * @return
     */
    public static List<String> getIPListByDNS(String host)  {
        try {
            InetAddress[] addresses= Address.getAllByName(host);
            List<String> ipList=new ArrayList<String>();
            for (InetAddress inetAddress:addresses){
                ipList.add(inetAddress.getHostAddress());
            }
            return ipList;
        }catch (Exception e){
            logger.error("getIPListByDNS",e);
            return null;
        }
    }

    /**
     * 通过dns解析域名得到IP
     * @param host 域名
     * @return
     */
    public static String getIPByDNS(String host)  {
        Integer i=0;
        while (true) {
            try {
                InetAddress[] addresses = Address.getAllByName(host);
                List<String> ipList = new ArrayList<String>();
                for (InetAddress inetAddress : addresses) {
                    ipList.add(inetAddress.getHostAddress());
                }
                if (ipList.size() > 0) {
                    return ipList.get(0);
                }
            } catch (Exception e) {
                logger.error("getIPListByDNS", e);
            }
            i++;
            ThreadUtil.sleep(1000);
            if (i>3){
                return null;
            }
        }
    }


    public static void main(String[] args) {
        for (int i=0;i<10000;i++) {
            Long beg=System.currentTimeMillis();
            System.out.println(getIPByDNS("mvn.sunboxsd.top"));
            Long end =System.currentTimeMillis();
            System.out.println(end-beg);
        }
    }

}
