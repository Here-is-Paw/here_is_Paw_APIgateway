package com.ll.apigateway.model.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public class TokenDto {

  public static class LoginUser implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private String nickname;
    private String role;

    public LoginUser() {
    }

    public LoginUser(Long id, String username) {
      this.id = id;
      this.username = username;
    }

    public LoginUser(Long id, String username, String nickname, String role) {
      this.id = id;
      this.username = username;
      this.nickname = nickname;
      this.role = role;
    }

    public Long getId() {
      return id;
    }

    public void setId(Long id) {
      this.id = id;
    }

    public String getUsername() {
      return username;
    }

    public void setUsername(String username) {
      this.username = username;
    }

    public String getNickname() {
      return nickname;
    }

    public void setNickname(String nickname) {
      this.nickname = nickname;
    }

    public String getRole() {
      return role;
    }

    public void setRole(String role) {
      this.role = role;
    }
  }

  public static class TokenResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private String accessToken;
    private String apiKey;

    public TokenResponse() {
    }

    public TokenResponse(String accessToken, String apiKey) {
      this.accessToken = accessToken;
      this.apiKey = apiKey;
    }

    public String getAccessToken() {
      return accessToken;
    }

    public void setAccessToken(String accessToken) {
      this.accessToken = accessToken;
    }

    public String getApiKey() {
      return apiKey;
    }

    public void setApiKey(String apiKey) {
      this.apiKey = apiKey;
    }
  }

  public static class TokenEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userId;
    private String username;
    private String eventType; // TOKEN_GENERATED, TOKEN_INVALIDATED
    private LocalDateTime timestamp;

    public TokenEvent() {
    }

    public TokenEvent(Long userId, String username, String eventType, LocalDateTime timestamp) {
      this.userId = userId;
      this.username = username;
      this.eventType = eventType;
      this.timestamp = timestamp;
    }

    public Long getUserId() {
      return userId;
    }

    public void setUserId(Long userId) {
      this.userId = userId;
    }

    public String getUsername() {
      return username;
    }

    public void setUsername(String username) {
      this.username = username;
    }

    public String getEventType() {
      return eventType;
    }

    public void setEventType(String eventType) {
      this.eventType = eventType;
    }

    public LocalDateTime getTimestamp() {
      return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
      this.timestamp = timestamp;
    }
  }
}
