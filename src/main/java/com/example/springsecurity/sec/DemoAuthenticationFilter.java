package com.example.springsecurity.sec;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;

import com.example.springsecurity.app.AppProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class DemoAuthenticationFilter extends AuthenticationWebFilter {

  public DemoAuthenticationFilter(
      DemoAuthorizationReactiveAuthenticationManager authenticationManager,
      AfterAuthenticateSuccess afterAuthenticateSuccess,
      ServerSecurityContextRepository serverSecurityContextRepository,
      IsItAauthenticationRequest isItAauthenticationRequest,
      ExtractAuthenticateInfoFromExchange extractAuthenticateInfoFromExchange,
      IfAuthFailure ifAuthFailure,
      AppProperties appProperties,
      ObjectMapper objectMapper) {
    super(authenticationManager);
    setRequiresAuthenticationMatcher(isItAauthenticationRequest);
    setServerAuthenticationConverter(extractAuthenticateInfoFromExchange);
    setAuthenticationSuccessHandler(afterAuthenticateSuccess);
    setAuthenticationFailureHandler(ifAuthFailure);
    setSecurityContextRepository(serverSecurityContextRepository);
  }

  @Override
  protected Mono<Void> onAuthenticationSuccess(Authentication authentication,
      WebFilterExchange webFilterExchange) {
    FinalAuthenticatedToken authenticationResult = (FinalAuthenticatedToken) authentication;
    return super.onAuthenticationSuccess(authenticationResult, webFilterExchange);
  }
}
