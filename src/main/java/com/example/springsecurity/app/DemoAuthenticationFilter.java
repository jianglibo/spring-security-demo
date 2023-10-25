package com.example.springsecurity.app;

import java.util.stream.Stream;

import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;

import com.example.springsecurity.app.DemoAuthorizationReactiveAuthenticationManager.DemoAuthAuthenticationToken;
import com.example.springsecurity.app.DemoAuthorizationReactiveAuthenticationManager.DemoServerAuthenticationConverter;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class DemoAuthenticationFilter extends AuthenticationWebFilter {

  public DemoAuthenticationFilter(ReactiveAuthenticationManager authenticationManager,
      ServerAuthenticationSuccessHandler serverAuthenticationSuccessHandler,
      ServerAuthenticationFailureHandler serverAuthenticationFailureHandler,
      AppProperties appProperties,
      String contextPath,
      ObjectMapper objectMapper) {
    super(authenticationManager);
    setRequiresAuthenticationMatcher(new DemoAuthenticatedServerWebExchangeMatcher(contextPath));
    setServerAuthenticationConverter(new DemoServerAuthenticationConverter(objectMapper));
    setAuthenticationSuccessHandler(serverAuthenticationSuccessHandler);
    setAuthenticationFailureHandler(serverAuthenticationFailureHandler);
  }

  @Override
  protected Mono<Void> onAuthenticationSuccess(Authentication authentication,
      WebFilterExchange webFilterExchange) {
    DemoAuthAuthenticationToken authenticationResult = (DemoAuthAuthenticationToken) authentication;
    return super.onAuthenticationSuccess(authenticationResult, webFilterExchange);
  }

  /**
   * This is the matcher that determines if the filter should be applied to the
   * request.
   */
  public static class DemoAuthenticatedServerWebExchangeMatcher implements ServerWebExchangeMatcher {

    private final String[] matchPathes;
    AntPathMatcher antPathMatcher = new AntPathMatcher();

    /**
     * s
     * 
     * @param matchPathes
     */
    public DemoAuthenticatedServerWebExchangeMatcher(String contextPath) {
      this.matchPathes = new String[] { contextPath + "custom-login-page",
          contextPath + "skipcsrf/custom-login-page" };
    }

    /**
     * {{authentication.filter.matcher.description}}
     */
    @Override
    public Mono<MatchResult> matches(ServerWebExchange exchange) {
      return MatchResult.match().filter(matchResult -> {
        if (exchange.getRequest().getHeaders().containsKey("X-API-KEY")) {
          return true;
        }
        if (exchange.getRequest().getMethod() != HttpMethod.POST) {
          return false;
        }
        String path = exchange.getRequest().getPath().value();
        log.info("authpath: {}, matcherpath1: {}, matcherpath2: {}", path, matchPathes[0],
            matchPathes[1]);
        return Stream.of(matchPathes).anyMatch(path::equals);
      }).switchIfEmpty(MatchResult.notMatch());
    }
  }
}
