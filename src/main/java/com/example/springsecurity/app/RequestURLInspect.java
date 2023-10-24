package com.example.springsecurity.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class RequestURLInspect implements WebFilter, Ordered {

  @Autowired
  AppProperties appProperties;

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    log.error("*****REQUEST URL*****: {}", exchange.getRequest().getURI().toString());
    return chain.filter(exchange);
  }

  @Override
  public int getOrder() {
    return 50;
  }
}
