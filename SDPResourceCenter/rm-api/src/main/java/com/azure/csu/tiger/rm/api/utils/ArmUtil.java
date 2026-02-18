package com.azure.csu.tiger.rm.api.utils;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.AzureResourceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ArmUtil {

    @Autowired
    private TokenCredential tokenCredential;
    @Autowired
    private AzureProfile profile;

    @Value("${azure.default-subscriptionId}")
    private String defaultSubscriptionId;

    @Value("${azure.dns-subscriptionId}")
    private String dnsSubscriptionId;

    private ConcurrentHashMap<String, AzureResourceManager> map = new ConcurrentHashMap();

    private static final ThreadLocal<AzureResourceManager> armThreadLocal = new ThreadLocal<>();

    private static final ThreadLocal<String> subThreadLocal = new ThreadLocal<>();

    @PostConstruct
    private void init() {
        map.put("default", AzureResourceManager.configure().authenticate(tokenCredential, profile).withSubscription(defaultSubscriptionId));
        map.put("dnsZone", AzureResourceManager.configure().authenticate(tokenCredential, profile).withSubscription(dnsSubscriptionId));
    }

//    public void setAzureResourceManager(String subscriptionId) {
//        if (!map.contains(subscriptionId)) {
//            map.put(subscriptionId, AzureResourceManager.configure().authenticate(tokenCredential, profile).withSubscription(subscriptionId));
//        }
//    }

    public AzureResourceManager getAzureResourceManager(String subscriptionId) {
        if (!map.containsKey(subscriptionId)) {
            map.put(subscriptionId, AzureResourceManager.configure().authenticate(tokenCredential, profile).withSubscription(subscriptionId));
        }
        return map.get(subscriptionId);
    }

    public static AzureResourceManager getArmData() {
        return armThreadLocal.get();
    }

    public static void setArmData(AzureResourceManager data) {
        armThreadLocal.set(data);
    }

    public static String getSubData() {
        return subThreadLocal.get();
    }

    public static void setSubData(String data) {
        subThreadLocal.set(data);
    }

    public static void clear() {
        armThreadLocal.remove();
        subThreadLocal.remove();
    }
}
