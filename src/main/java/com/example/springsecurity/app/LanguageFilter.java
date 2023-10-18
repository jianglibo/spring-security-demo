package com.example.springsecurity.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class LanguageFilter implements WebFilter {


  @Autowired
  AppProperties appProperties;

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    return exchange.getSession().map(ws -> {
      String l = ws.getAttributeOrDefault("lang", "en");
      exchange.getAttributes().put("lang", l);
      log.debug("got lang from session: {}", l);
      return l;
    }).then(chain.filter(exchange));
  }
}
