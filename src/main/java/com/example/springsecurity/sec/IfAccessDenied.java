package com.example.springsecurity.sec;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.security.web.server.authorization.ServerWebExchangeDelegatingServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * You could choose to use the
 * {@link ServerWebExchangeDelegatingServerAccessDeniedHandler}
 * But also choose to do it all by youself.
 * 
 */
@Component
@Slf4j
public class IfAccessDenied implements ServerAccessDeniedHandler {

	@Override
	public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException denied) {
		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(HttpStatus.SEE_OTHER);
		URI newLocation = URI
				.create(exchange.getRequest().getPath().contextPath().value()
						+ "/custom-access-deny-page?url="
						+ exchange.getRequest().getPath().value());
		response.getHeaders().setLocation(newLocation);
		return exchange.getResponse().setComplete();

		// default
		// exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
		// return exchange.getResponse().setComplete();
	}

}
