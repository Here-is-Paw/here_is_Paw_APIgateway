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

@Configuration
@Slf4j
public class GatewayConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  public GatewayConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
  }

  @Value("${services.auth.url}")
  private String authServiceUrl;

  @Value("${services.noti.url}")
  private String notiServiceUrl;

  @Bean
  public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
    return builder.routes()
        // 인증 서비스 라우팅 (토큰 검증 필터 미적용)
        .route("auth-service", r -> r.path("/auth/**", "/api/v1/members/login",
                "/api/v1/members/logout", "/api/v1/members/signup",
                "/oauth2/authorization/**", "/api/v1/profile/**")
            .filters(f -> f.filter((exchange, chain) -> {
              log.info("Auth service route matched: {}", exchange.getRequest().getURI());
              return chain.filter(exchange);
            }))
            .uri(authServiceUrl))

        // 알림 서비스 라우팅 (토큰 검증 필터 적용)
        .route("noti-service", r -> r.path("/api/v1/noti")
            .filters(f -> f
                .filter((exchange, chain) -> {
                  log.info("Noti service route matched: {}", exchange.getRequest().getURI());
                  return chain.filter(exchange);
                })
                .filter(jwtAuthenticationFilter)
            )
            .uri(notiServiceUrl))

        // 채팅 서비스 라우팅 (필터 미적용)
        .route("chat-service", r -> r.path("/api/v1/chat/**")
            .uri("http://chat-service:8082"))

        // Swagger UI
        .route("swagger-ui", r -> r.path("/swagger-ui/**")
            .uri("http://api-docs:8083"))

//        // 기타 서비스 라우팅 (JWT 검증 필터 적용)
//        .route("other-services", r -> r.path("/api/**")
//            .filters(f -> f.filter(jwtAuthenticationFilter))
//            .uri("http://service-routing:8084"))

        .build();
  }
}
