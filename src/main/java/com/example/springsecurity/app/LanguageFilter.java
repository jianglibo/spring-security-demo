package com.example.springsecurity.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class LanguageFilter implements WebFilter, Ordered {

  @Autowired
  AppProperties appProperties;

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    return exchange.getSession().map(ws -> {
      String l = ws.getAttribute("lang");
      if (l == null) {
        HttpCookie cookie = exchange.getRequest().getCookies().getFirst("lang");
        if (cookie != null) {
          l = cookie.getValue();
        } else {
          // retrieve from the Accept-Language header
          l = exchange.getRequest().getHeaders().getFirst("Accept-Language");
          if (l != null) {
            l = l.split(",")[0];
            if (l.length() > 2) {
              l = l.substring(0, 2);
            }
          } else {
            l = "zh";
          }
        }
      }
      exchange.getAttributes().put("lang", l);
      log.debug("got lang from session: {}", l);
      return l;
    }).then(chain.filter(exchange));
  }

  @Override
  public int getOrder() {
    // Ordered.LOWEST_PRECEDENCE;
    return 100;
  }
}
