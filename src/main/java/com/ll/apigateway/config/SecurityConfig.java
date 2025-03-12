package com.ll.apigateway.config;

import java.util.Arrays;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

//  @Bean
//  public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
//    http
//        .csrf(ServerHttpSecurity.CsrfSpec::disable)
//        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
//        .authorizeExchange(exchanges -> exchanges
//            .pathMatchers(
//                "/auth/**"
//                , "/api/v1/members/login"
//                , "/api/v1/members/logout"
//                , "/api/v1/members/signup"
//                , "/oauth2/authorization/**"
//                , "/api/v1/profile/**"
//                , "/swagger-ui/**"
//                , "/api/v1/chat/**"
//                ).permitAll()
//            .anyExchange().authenticated()
//        )
//    ;
//
//    return http.build();
//  }

  @Bean
  public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
    http
        .csrf(ServerHttpSecurity.CsrfSpec::disable)
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .authorizeExchange(exchanges -> exchanges
            .pathMatchers(
                "/auth/**",
                "/api/v1/members/login",
                "/api/v1/members/logout",
                "/api/v1/members/signup",
                "/api/v1/members/me",
                "/api/v1/profile/**",
                "/h2-console/**",
                "/oauth2/authorization/**",
                "/api/v1/chat/**"
            ).permitAll()
//            .pathMatchers("/api/**").authenticated()
            .anyExchange().permitAll()
        )
        .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
        .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
        // WebFlux에서는 다음과 같이 설정합니다
        .headers(headers ->
            headers.frameOptions(frameOptions -> frameOptions.mode(
                XFrameOptionsServerHttpHeadersWriter.Mode.SAMEORIGIN))
        )
        .securityContextRepository(NoOpServerSecurityContextRepository.getInstance());

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList(
        "http://localhost:5173",
        "https://www.pawpaw.kknaks.site",
        "https://pawpaw.kknaks.site"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}