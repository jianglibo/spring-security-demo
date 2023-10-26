package com.example.springsecurity.sec;

import java.net.URI;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.logout.RedirectServerLogoutSuccessHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * read the {@link RedirectServerLogoutSuccessHandler} as an example.
 */
@Component
@Slf4j
public class AfterLogoutSuccessfully implements ServerLogoutSuccessHandler {

	private RedirectServerLogoutSuccessHandler handler;

	public AfterLogoutSuccessfully() {
		handler = new RedirectServerLogoutSuccessHandler();
		handler.setLogoutSuccessUrl(URI.create("/"));
	}

	@Override
	public Mono<Void> onLogoutSuccess(WebFilterExchange exchange, Authentication authentication) {
		return this.handler.onLogoutSuccess(exchange, authentication);
	}

}
