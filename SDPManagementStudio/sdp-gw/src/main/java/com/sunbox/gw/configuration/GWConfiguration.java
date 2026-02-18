package com.sunbox.gw.configuration;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GWConfiguration {


/*    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder){

        RouteLocator routeLocator = builder.routes()
//                .route(p -> p.path("/api/**")
//                        .filters(f ->f.hystrix(config -> config.setName("hystrix-common").setFallbackUri("forward:/fallback")).stripPrefix(1))
//                        .uri("lb://IGS-GW/**")
//                        .id("route_invoke")
//
//                )
                .route(p->p.path("/api/order/**")
                        .filters(f->f.hystrix(config->config.setName("hx-order").setFallbackUri("forward:/fallback")).stripPrefix(2))
                        .uri("lb://IGS-ORDER/**")
                        .id("toute_order"))

                .route(p->p.path("/api/mq/**")
                        .filters(f->f.hystrix(config->config.setName("hx-mq").setFallbackUri("forward:/fallback")).stripPrefix(2))
                        .uri("lb://IGSMQ/**")
                        .id("route_mq"))

                .route(p->p.path("/api/user/**")
                        .filters(f->f.hystrix(config->config.setName("hx-mq").setFallbackUri("forward:/fallback")).stripPrefix(2))
                        .uri("lb://IGS-USER/**")
                        .id("route_user"))

                .build();
        return routeLocator;
    }*/

//    @Bean
//    public GlobalFilter globalFilter() {
//        return new com.sunbox.filter.GlobalFilter();
//    }


}
