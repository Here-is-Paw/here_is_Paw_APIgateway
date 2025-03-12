package com.ll.apigateway.config;

import com.ll.apigateway.filter.JwtAuthenticationFilter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class GatewayConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${services.auth.url}")
    private String authServiceUrl;

    @Value("${services.post.url}")
    private String postServiceUrl;

    @Value("${services.noti.url}")
    private String notiServiceUrl;

    @Value("${services.chat.url}")
    private String chatServiceUrl;

    @Value("${services.payment.url}")
    private String paymentServiceUrl;

    @Value("${services.search.url}")
    private String searchServiceUrl;

    @Value("${services.carecenter.url}")
    private String carecenterServiceUrl;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // 인증 서비스 라우팅 (토큰 검증 필터 미적용)
                .route("auth-unless-token-service", r -> r.path("/auth/**", "/api/v1/members/login",
                                "/api/v1/members/logout", "/api/v1/members/signup",
                                "/oauth2/authorization/**", "/api/v1/profile/**", "/login/oauth2/code/**")
                        .filters(f -> f.filter((exchange, chain) -> {
                            return chain.filter(exchange);
                        }))
                        .uri(authServiceUrl))
                .route("auth-service", r -> r.path("/api/v1/members/**", "/api/v1/mypets/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri(authServiceUrl))

                .route("noti-service", r -> r.path("/api/v1/noti/**", "/api/v1/sse/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri(notiServiceUrl))

                .route("post-services-get", r -> r.path("/api/v1/missings/**", "/api/v1/finding/**")
                        .and().method(HttpMethod.GET)
                        .filters(f -> f.filter((exchange, chain) -> {
                            log.info("Auth-sevice url: {}", postServiceUrl);
                            log.info("Auth service route matched: {}", exchange.getRequest().getURI());
                            return chain.filter(exchange);
                        }))
                        .uri(postServiceUrl))
                .route("post-services", r -> r.path("/api/v1/missings/**", "/api/v1/finding/**","/api/v1/userPosts/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri(postServiceUrl))

                .route("chat-services", r -> r.path("/api/v1/chat/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri(chatServiceUrl))
                .route("chat-websocket", r -> r.path("/ws/**")
                        .filters(f -> f.setRequestHeader("X-Forwarded-Prefix", "/ws"))
                        .uri(chatServiceUrl))

                .route("payment-services", r -> r.path("/api/v1/payment/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri(paymentServiceUrl))

                .route("search-services", r -> r.path("/api/v1/searchPost/**", "/api/v1/searchMember/**")
                        .filters(f -> f.filter((exchange, chain) -> {
                            return chain.filter(exchange);
                        }))
                        .uri(searchServiceUrl))

                .route("carecenter-service", r -> r.path( "/api/v1/care-center")
                    .filters(f -> f.filter((exchange, chain) -> {
                        return chain.filter(exchange);
                    }))
                    .uri(carecenterServiceUrl))

                .build();
    }
}
//
//        .route("chat-services", r -> r.path("/api/v1/chat/**", "/api/v1/finding/**")
//            .filters(f -> f
//    .filter((exchange, chain) -> {
//    log.info("noti-service url: {}", chatServiceUrl);
//                  log.info("Noti service route matched: {}", exchange.getRequest().getURI());
//
//    // 모든 헤더 로깅
//    exchange.getRequest().getHeaders().forEach((key, value) -> {
//    log.info("Incoming Header - {}: {}", key, value);
//                  });
//
//                      return chain.filter(exchange);
//                })
//                    .filter((exchange, chain) -> {
//    log.info("Entering JwtAuthenticationFilter");
//                  return jwtAuthenticationFilter.filter(exchange, chain);
//                })
//                    .filter((exchange, chain) -> {
//    log.info("Passed JwtAuthenticationFilter");
//                  return chain.filter(exchange);
//                })
//                    )
//                    .uri(chatServiceUrl))