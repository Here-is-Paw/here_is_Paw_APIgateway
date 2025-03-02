package com.ll.apigateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.springframework.cloud.gateway.filter.GlobalFilter;

@Component
public class CustomGlobalFilter implements GlobalFilter, Ordered {

  private static final Logger log = LoggerFactory.getLogger(CustomGlobalFilter.class);

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    ServerHttpRequest request = exchange.getRequest();

    log.info("Global Filter - Request URI: {}", request.getURI());
    log.info("Global Filter - Request Path: {}", request.getPath());
    log.info("Global Filter - Request Method: {}", request.getMethod());

    // 요청 타임스탬프 추가
    ServerHttpRequest mutatedRequest = request.mutate()
        .header("X-Request-Time", String.valueOf(System.currentTimeMillis()))
        .build();

    return chain.filter(exchange.mutate().request(mutatedRequest).build())
        .then(Mono.fromRunnable(() -> {
          log.info("Global Filter - Response Status: {}", exchange.getResponse().getStatusCode());
        }));
  }

  @Override
  public int getOrder() {
    // JwtAuthenticationFilter보다 먼저 실행 (-100보다 작은 값)
    return -200;
  }
}