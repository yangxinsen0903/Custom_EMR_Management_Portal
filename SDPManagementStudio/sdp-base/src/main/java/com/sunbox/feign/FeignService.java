package com.sunbox.feign;

public interface FeignService {

    /**
     * @fun 通过url创建feign客户端实例
     * @param apiType feign接口类
     * @param url 动态url，包含协议、ip、端口、根目录，如:"http://127.0.0.1:30400/order"
     * @return
     */
    public <T> T instanceByUrl(Class<T> apiType, String url);

    /**
     * @fun 通过服务名创建url
     * @param apiType feign接口类
     * @param name 动态名称，包含协议、名称、根目录，如:"IGS-ORDER"
     * @return
     */
    public <T> T instanceByName(Class<T> apiType, String name);

}