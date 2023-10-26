package com.example.springsecurity.sec;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.springsecurity.htmx.HxRequestHeaders;
import com.example.springsecurity.htmx.HxResponseHeaders;
import com.example.springsecurity.htmx.HxResponseUtil;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class AfterAuthenticateSuccess implements ServerAuthenticationSuccessHandler {

	@Autowired
	HxResponseUtil hxResponseUtil;
	@Autowired
	ServerRequestCache serverRequestCache;

	@Override
	public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
		ServerWebExchange exchange = webFilterExchange.getExchange();
		String contextPath = exchange.getRequest().getPath().contextPath().value();
		return serverRequestCache.getRedirectUri(exchange)
				.defaultIfEmpty(URI.create(contextPath))
				.flatMap(uri -> {
					UriComponents uric = UriComponentsBuilder.fromUri(uri).replacePath(contextPath + uri.getPath())
							.build();
					uri = uric.toUri();

					ServerHttpResponse response = exchange.getResponse();
					if (HxRequestHeaders.isHxRequest(exchange.getRequest())) {
						String reqPath = exchange.getRequest().getPath().value();
						String saveUri = uri.toString();
						if (!saveUri.startsWith(contextPath)) {
							saveUri = contextPath + saveUri;
						}
						if (reqPath.endsWith("/custom-login-page")) {
							response.getHeaders().add(HxResponseHeaders.REDIRECT.getValue(), saveUri);
							response.setStatusCode(HttpStatus.NO_CONTENT);
						} else {
							return webFilterExchange.getChain().filter(exchange);
						}
					} else {
						response.setStatusCode(HttpStatus.SEE_OTHER);
						response.getHeaders().setLocation(uri);
					}
					return Mono.empty();
				});

	}

}
