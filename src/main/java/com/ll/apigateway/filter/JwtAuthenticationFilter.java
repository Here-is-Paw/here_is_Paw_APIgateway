package com.ll.apigateway.filter;

import com.ll.apigateway.model.dto.TokenDto.LoginUser;
import com.ll.apigateway.service.JwtService;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter implements GatewayFilter {

  @Value("${services.auth.url}")
  private String memberServiceUrl;

  private final JwtService jwtService;
  private final WebClient.Builder webClientBuilder;
  private final List<String> excludedPaths = List.of(
      "/api/v1/members/login",
      "/api/v1/members/logout",
      "/api/v1/members/signup",
      "/api/v1/members/token/refresh", // 토큰 재발급 엔드포인트 추가
      "/api/v1/profile/",
      "/swagger-ui/index.html",
      "/login/oauth2/code/**"
  );

//  public JwtAuthenticationFilter(JwtService jwtService) {
//    this.jwtService = jwtService;
//  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    ServerHttpRequest request = exchange.getRequest();

    log.info("Global Filter - Request URI: {}", request.getURI());
    log.info("Global Filter - Request Method: {}", request.getMethod());

    String path = request.getURI().getPath();

    // 제외 경로 확인
    if (excludedPaths.stream().anyMatch(path::startsWith)) {
      return chain.filter(exchange);
    }

    // 토큰과 API 키 추출
    Optional<String> accessToken = extractAccessToken(exchange);
    Optional<String> apiKey = extractApiKey(exchange);

    // 토큰과 API 키가 모두 없는 경우
    if (accessToken.isEmpty() && apiKey.isEmpty()) {
      return onError(exchange, "Missing authentication token", HttpStatus.UNAUTHORIZED);
    }

    // 액세스 토큰 검증
    if (accessToken.isPresent()) {
      try {
        Map<String, Object> claims = jwtService.validateAndParseToken(accessToken.get());
        if (claims != null) {
          LoginUser loginUser = new LoginUser(
              Long.parseLong(claims.get("id").toString()),
              claims.get("username").toString()
          );

          // 사용자 정보를 헤더에 추가
          return addUserInfoToHeaders(exchange, chain, loginUser);
        }
      } catch (Exception e) {
        // 토큰이 유효하지 않고 API 키가 있는 경우 토큰 재발급 진행
        if (apiKey.isPresent()) {
          return refreshTokenAndContinue(exchange, chain, apiKey.get());
        }
        return onError(exchange, "Invalid authentication token", HttpStatus.UNAUTHORIZED);
      }
    }

    // 토큰이 없지만 API 키가 있는 경우 토큰 재발급 진행
    if (apiKey.isPresent()) {
      return refreshTokenAndContinue(exchange, chain, apiKey.get());
    }

    return onError(exchange, "Invalid authentication token", HttpStatus.UNAUTHORIZED);
  }

  private Mono<Void> refreshTokenAndContinue(ServerWebExchange exchange, GatewayFilterChain chain, String apiKey) {
    ServerHttpRequest originalRequest = exchange.getRequest();
    URI originalUri = originalRequest.getURI();
    String originalPath = originalUri.getPath();
    String originalQuery = originalUri.getQuery() != null ? "?" + originalUri.getQuery() : "";

    // 토큰 리프레시 후 원래 URL로 리다이렉트하도록 파라미터 추가
    URI tokenRefreshUri = UriComponentsBuilder
        .fromUriString("/api/v1/members/token/refresh")
        .queryParam("apiKey", apiKey)
        .queryParam("redirectUri", originalPath + originalQuery)
        .build()
        .toUri();

    ServerHttpResponse response = exchange.getResponse();
    response.setStatusCode(HttpStatus.SEE_OTHER);  // 303 리다이렉트는 GET 메서드를 강제함
    response.getHeaders().set(HttpHeaders.LOCATION, tokenRefreshUri.toString());

    log.info("Redirecting to token refresh endpoint: {} with redirect back to {}",
        tokenRefreshUri, originalPath + originalQuery);
    return response.setComplete();
  }
//
//  private Mono<Void> refreshTokenAndContinue(ServerWebExchange exchange, GatewayFilterChain chain, String apiKey) {
//    ServerHttpRequest originalRequest = exchange.getRequest();
//    URI originalUri = originalRequest.getURI();
//
//    log.info("Refreshing token for API key: {}", apiKey);
//
//    // WebClient를 사용하여 토큰 리프레시 요청
//    return webClientBuilder.build()
//        .get()
//        .uri(memberServiceUrl + "/api/v1/members/token/refresh?apiKey=" + apiKey)
//        .retrieve()
//        .bodyToMono(Map.class)
//        .flatMap(responseBody -> {
//          log.info("Token refresh response: {}", responseBody);
//          String newAccessToken = (String) responseBody.get("accessToken");
//
//          if (newAccessToken != null) {
//            // 원래 요청에 새 토큰 추가
//            ServerHttpRequest newRequest = originalRequest.mutate()
//                .header("Authorization", "Bearer " + apiKey + " " + newAccessToken)
//                .build();
//
//            // 쿠키 설정
//            ServerHttpResponse response = exchange.getResponse();
//            ResponseCookie cookie = ResponseCookie.from("accessToken", newAccessToken)
//                .path("/")
//                .maxAge(3600)
//                .httpOnly(true)
//                .build();
//            response.getHeaders().add(HttpHeaders.SET_COOKIE, cookie.toString());
//
//            // 새 요청으로 교환 업데이트
//            ServerWebExchange newExchange = exchange.mutate()
//                .request(newRequest)
//                .build();
//
//            // 원래 체인으로 계속 진행
//            return chain.filter(newExchange);
//          }
//
//          return onError(exchange, "Failed to refresh token", HttpStatus.UNAUTHORIZED);
//        })
//        .onErrorResume(e -> {
//          log.error("Error refreshing token", e);
//          return onError(exchange, "Error refreshing token: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
//        });
//  }

  private Mono<Void> addUserInfoToHeaders(ServerWebExchange exchange, GatewayFilterChain chain,
      LoginUser loginUser) {
    log.info("Adding user info to headers: {}", loginUser);
    log.info("Adding user info to headers: {}", loginUser.getId());
    // Kafka 이벤트로 전달할 정보만 헤더에 추가
    ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
        .header("X-User-Id", String.valueOf(loginUser.getId()))
        .header("X-Username", loginUser.getUsername())
        .build();

    return chain.filter(exchange.mutate().request(mutatedRequest).build());
  }

  private Optional<String> extractAccessToken(ServerWebExchange exchange) {
    // Authorization 헤더에서 추출
    List<String> authHeaders = exchange.getRequest().getHeaders().get("Authorization");
//    log.debug("Authorization headers: {}", authHeaders);

    if (authHeaders != null && !authHeaders.isEmpty()) {
      String authHeader = authHeaders.get(0);
//      log.debug("Auth header: {}", authHeader);

      if (authHeader.startsWith("Bearer ")) {
        // Bearer 다음의 모든 내용을 토큰으로 간주
        String tokenContent = authHeader.substring(7);
//        log.debug("Token content: {}", tokenContent);

        // 간단히 공백으로 구분
        String[] tokenParts = tokenContent.split(" ", 2);
//        log.debug("Token parts length: {}", tokenParts.length);

        if (tokenParts.length == 2) {
//          log.debug("Using second part as access token");
          return Optional.of(tokenParts[1]);
        } else if (tokenParts.length == 1) {
//          log.debug("Using entire token content as access token");
          return Optional.of(tokenContent);
        }
      }
    }

    // 쿠키에서 추출
//    log.debug("Cookies: {}", exchange.getRequest().getCookies());
    HttpCookie cookie = exchange.getRequest().getCookies().getFirst("accessToken");
    if (cookie != null) {
//      log.debug("Found accessToken cookie: {}", cookie.getValue());
      return Optional.of(cookie.getValue());
    }

    log.debug("No access token found");
    return Optional.empty();
  }

  private Optional<String> extractApiKey(ServerWebExchange exchange) {
    // Authorization 헤더에서 추출
    List<String> authHeaders = exchange.getRequest().getHeaders().get("Authorization");
    if (authHeaders != null && !authHeaders.isEmpty()) {
      String authHeader = authHeaders.get(0);
      if (authHeader.startsWith("Bearer ")) {
        String[] tokenParts = authHeader.substring(7).split(" ", 2);
        if (tokenParts.length == 2) {
          return Optional.of(tokenParts[0]);
        }
      }
    }

    // 쿠키에서 추출
    HttpCookie cookie = exchange.getRequest().getCookies().getFirst("apiKey");
    if (cookie != null) {
      return Optional.of(cookie.getValue());
    }

    return Optional.empty();
  }

  private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
    ServerHttpResponse response = exchange.getResponse();
    response.setStatusCode(status);
    return response.setComplete();
  }
}
