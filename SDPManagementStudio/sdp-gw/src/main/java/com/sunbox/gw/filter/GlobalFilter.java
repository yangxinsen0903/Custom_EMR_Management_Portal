package com.sunbox.gw.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.server.RequestPath;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class GlobalFilter implements org.springframework.cloud.gateway.filter.GlobalFilter, Ordered {
    Logger logger = LoggerFactory.getLogger(GlobalFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        RequestPath path = exchange.getRequest().getPath();
//        MultiValueMap<String, String> queryParams = exchange.getRequest().getQueryParams();
        logger.info("GlobalFilter 请求地址：" + path );
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1000;
    }
}
