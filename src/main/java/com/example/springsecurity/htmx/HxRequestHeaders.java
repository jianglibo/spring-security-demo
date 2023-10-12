package com.example.springsecurity.htmx;

import java.util.List;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.reactive.function.server.ServerRequest;

import lombok.Getter;

public enum HxRequestHeaders {

  CURRENT_URL("Hx-Current-Url"), REQUEST("Hx-Request"), TARGET("Hx-Target");

  public static boolean isHxRequest(ServerRequest req) {
    return req.headers().header(REQUEST.getValue()).stream().anyMatch("true"::equalsIgnoreCase);
  }

  public static boolean isHxRequest(ServerHttpRequest req) {
    List<String> values = req.getHeaders().get(REQUEST.getValue());
    return values != null
        && values.stream().anyMatch("true"::equalsIgnoreCase);
  }

  @Getter
  private String value;

  private HxRequestHeaders(String value) {
    this.value = value;
  }

}
