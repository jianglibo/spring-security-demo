package com.example.springsecurity.sec;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationEntryPoint;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class WhenNeedAuthentication implements ServerAuthenticationEntryPoint {

	RedirectServerAuthenticationEntryPoint entryPoint = new RedirectServerAuthenticationEntryPoint(
			"/custom-login-page");

	public WhenNeedAuthentication(ServerRequestCache serverRequestCache) {
		entryPoint = new RedirectServerAuthenticationEntryPoint(
				"/custom-login-page");
		entryPoint.setRequestCache(serverRequestCache);

	}

	@Override
	public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
		return entryPoint.commence(exchange, ex);
	}

}
