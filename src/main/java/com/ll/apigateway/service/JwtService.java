package com.ll.apigateway.service;

import com.ll.apigateway.model.dto.TokenDto.LoginUser;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class JwtService {

  private final JwtUtil jwtUtil;
  private final WebClient.Builder webClientBuilder;
  private final KafkaTemplate<String, Map<String, Object>> kafkaTemplate;

  private JwtService(JwtUtil jwtUtil, WebClient.Builder webClientBuilder, KafkaTemplate<String, Map<String, Object>> kafkaTemplate) {
    this.jwtUtil = jwtUtil;
    this.webClientBuilder = webClientBuilder;
    this.kafkaTemplate = kafkaTemplate;
  }

  @Value("${services.auth.url}")
  private String authServiceUrl;

  @Value("${kafka.topics.token-events}")
  private String tokenEventsTopic;

  public Map<String, Object> validateAndParseToken(String token) {
    return jwtUtil.parseToken(token);
  }

//  public String generateAccessToken(LoginUser loginUser) {
//    Map<String, Object> claims = new HashMap<>();
//    claims.put("id", loginUser.getId());
//    claims.put("username", loginUser.getUsername());
//
//    String token = jwtUtil.generateToken(claims);
//
//    // 토큰 발급 이벤트 Kafka로 발행
//    Map<String, Object> tokenEvent = new HashMap<>();
//    tokenEvent.put("userId", loginUser.getId());
//    tokenEvent.put("username", loginUser.getUsername());
//    tokenEvent.put("eventType", "TOKEN_GENERATED");
//    tokenEvent.put("timestamp", System.currentTimeMillis());
//
//    kafkaTemplate.send(tokenEventsTopic, String.valueOf(loginUser.getId()), tokenEvent);
//
//    return token;
//  }

//  public Mono<LoginUser> getMemberByApiKey(String apiKey) {
//    return webClientBuilder.build()
//        .get()
//        .uri(authServiceUrl + "/api/v1/members/by-apikey?apiKey=" + apiKey)
//        .retrieve()
//        .bodyToMono(LoginUser.class);
//  }
//
//  public void invalidateToken(String token) {
//    try {
//      Map<String, Object> claims = jwtUtil.parseToken(token);
//      if (claims != null && claims.containsKey("id")) {
//        // 토큰 무효화 이벤트 Kafka로 발행
//        Map<String, Object> tokenEvent = new HashMap<>();
//        tokenEvent.put("userId", claims.get("id"));
//        tokenEvent.put("username", claims.get("username"));
//        tokenEvent.put("token", token);
//        tokenEvent.put("eventType", "TOKEN_INVALIDATED");
//        tokenEvent.put("timestamp", System.currentTimeMillis());
//
//        kafkaTemplate.send(tokenEventsTopic, claims.get("id").toString(), tokenEvent);
//      }
//    } catch (Exception e) {
//      // 토큰 파싱 오류 처리
//    }
//  }
}