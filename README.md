# here_is_Paw_APIgateway

- config: 라우팅 설정과 보안 설정을 포함
- filter: JWT 인증 필터와 전역 필터 구현
- handler: 예외 처리를 위한 핸들러
- service: JWT 관련 비즈니스 로직
- util: JWT 유틸리티 함수들
- model: DTO와 커스텀 예외 클래스

    ```markdown
        src/main/java/com/ll
        ├── HereIsPawApigatewayApplication.java
        ├── config
        │   ├── GatewayConfig.java
        │   └── SecurityConfig.java
        ├── filter
        │   ├── JwtAuthenticationFilter.java
        │   └── GlobalFilter.java
        ├── handler
        │   └── CustomExceptionHandler.java
        ├── service
        │   └── JwtService.java
        ├── util
        │   └── JwtUtil.java
        └── model
        ├── dto
        │   ├── ErrorResponse.java
        │   └── TokenDto.java
        └── exception
        └── CustomException.java
    ```
