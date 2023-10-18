package com.example.springsecurity.app;

import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

public class ContextPathFilter implements WebFilter, Ordered {

	private ServerProperties serverproperties;

	public ContextPathFilter(ServerProperties serverproperties) {
		this.serverproperties = serverproperties;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		// final String contextpath = serverproperties.getServlet().getContextPath();
		// final ServerHttpRequest request = exchange.getRequest();
		// if
		// (!request.getPath().modifyContextPath(contextpath).startswith(contextpath)) {
		// return chain.filter(
		// exchange.mutate()
		// .request(request
		// .mutate()
		// .contextPath(contextpath)
		// .build())
		// .build());
		// }
		return chain.filter(exchange);
	}

	@Override
	public int getOrder() {
		return -100;
	}
}
