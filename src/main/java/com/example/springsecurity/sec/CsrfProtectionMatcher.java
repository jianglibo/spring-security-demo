package com.example.springsecurity.sec;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.http.HttpMethod;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;


/**
 * You can read the {@link DefaultRequireCsrfProtectionMatcher}
 */
@Component
@Slf4j
public class CsrfProtectionMatcher implements ServerWebExchangeMatcher {

	private static final Set<HttpMethod> ALLOWED_METHODS = new HashSet<>(
			Arrays.asList(HttpMethod.GET, HttpMethod.HEAD, HttpMethod.TRACE, HttpMethod.OPTIONS));

	@Override
	public Mono<MatchResult> matches(ServerWebExchange exchange) {
		if (exchange.getRequest().getPath().value().startsWith("/skipcsrf/")) {
			return MatchResult.notMatch();
		}
		return Mono.just(exchange.getRequest()).flatMap((r) -> Mono.justOrEmpty(r.getMethod()))
				.filter(ALLOWED_METHODS::contains).flatMap((m) -> MatchResult.notMatch())
				.switchIfEmpty(MatchResult.match());
	}

}
