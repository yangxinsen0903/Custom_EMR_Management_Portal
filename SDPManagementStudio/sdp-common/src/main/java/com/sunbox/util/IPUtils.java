package com.sunbox.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class IPUtils {

    /**
     * Ip范围匹配
     * @param ipSection  地址段  192.168.1.1-192.168.1.10
     * @param ip Ip地址
     * @return
     */
    public static boolean ipIsValid(String ipSection, String ip) {
        if (ipSection == null)
            throw new NullPointerException("IP段不能为空！");
        if (ip == null)
            throw new NullPointerException("IP不能为空！");
        ipSection = ipSection.trim();
        ip = ip.trim();
        final String REGX_IP = "((25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]\\d|\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]\\d|\\d)";
        final String REGX_IPB = REGX_IP + "\\-" + REGX_IP;
        if (!ipSection.matches(REGX_IPB) || !ip.matches(REGX_IP))
            return false;
        int idx = ipSection.indexOf('-');
        String[] sips = ipSection.substring(0, idx).split("\\.");
        String[] sipe = ipSection.substring(idx + 1).split("\\.");
        String[] sipt = ip.split("\\.");
        long ips = 0L, ipe = 0L, ipt = 0L;
        for (int i = 0; i < 4; ++i) {
            ips = ips << 8 | Integer.parseInt(sips[i]);
            ipe = ipe << 8 | Integer.parseInt(sipe[i]);
            ipt = ipt << 8 | Integer.parseInt(sipt[i]);
        }
        if (ips > ipe) {
            long t = ips;
            ips = ipe;
            ipe = t;
        }
        return ips <= ipt && ipt <= ipe;
    }

    public static String getIpAddress() {
        String ip = "";
        try {
            final Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements()) {
                final NetworkInterface intf = en.nextElement();
                final String name = intf.getName();
                if (!name.contains("docker") && !name.contains("lo")) {
                    final Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses();
                    while (enumIpAddr.hasMoreElements()) {
                        final InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress()) {
                            final String ipaddress = inetAddress.getHostAddress();
                            if (ipaddress.contains("::") || ipaddress.contains("0:0:") || ipaddress.contains("0.0.")
                                    || ipaddress.contains("fe80")) {
                                continue;
                            }
                            ip = ipaddress;
                        }
                    }
                }
            }
        } catch (SocketException ex) {
            ip = "127.0.0.1";
        }
        return ip;
    }

    public static String getHostName(){
        try {
            InetAddress inetadd = InetAddress.getLocalHost();
            String name = inetadd.getHostName();
            return name;
        }
        catch(Exception u){
            return null;
        }
    }

}
