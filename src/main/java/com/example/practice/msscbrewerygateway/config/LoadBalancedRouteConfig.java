package com.example.practice.msscbrewerygateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("local-discovery")
@Configuration
public class LoadBalancedRouteConfig {
    @Bean
    public RouteLocator loadBalancedRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            .route(r -> r.path("/api/v1/beer*", "/api/v1/beer/*", "/api/v1/beerUpc/*")
                .uri("lb://mssc-beer-service")
            )
            .route(r -> r.path("/api/v1/customers*", "/api/v1/customers/*")
                .uri("lb://mssc-beer-order-service")
            )
            .route(r -> r.path("api/v1/beer/*/inventory")
                .filters(f -> f.circuitBreaker(config -> {
                    config.setName("inventory-circuitBreaker")
                        .setFallbackUri("fallback:/inventory-failover")
                        .setRouteId("/inventory-fallback");
                }))
                .uri("lb://mssc-beer-inventory-service")
            )
            .route(r -> r.path("/inventory-failover/**")
                .uri("lb://mssc-inventory-failover")
            )
            .build();
    }
}
