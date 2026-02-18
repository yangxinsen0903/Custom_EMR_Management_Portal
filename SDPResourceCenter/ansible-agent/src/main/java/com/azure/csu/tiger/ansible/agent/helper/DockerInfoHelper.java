package com.azure.csu.tiger.ansible.agent.helper;

public class DockerInfoHelper {

    public static String getPodInfo() {

//        String podName = System.getenv("HOSTNAME");
//        String podIP = System.getenv("KUBERNETES_PORT_443_TCP_ADDR");
        String podName = System.getenv("MY_POD_NAME");
        String podIP = System.getenv("MY_POD_IP");
        String appName = System.getenv("MY_APP_NAME");
        String formattedString = String.format("Pod Name: %s, Pod IP: %s, App Name: %s", podName, podIP, appName);
        return formattedString;
    }
}
