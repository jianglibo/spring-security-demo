package com.example.springsecurity.sec;

import java.util.stream.Stream;

import org.springframework.http.HttpMethod;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class IsItAauthenticationRequest implements ServerWebExchangeMatcher {

	private final String[] matchPathes = new String[] { "/custom-login-page", "/skipcsrf/custom-login-page" };

	@Override
	public Mono<MatchResult> matches(ServerWebExchange exchange) {
		return MatchResult.match().filter(matchResult -> {
			if (exchange.getRequest().getHeaders().containsKey("X-API-KEY")) {
				log.info("auth_matcher: got x-api-key {}", exchange.getRequest().getHeaders().containsKey("X-API-KEY"));
				return true;
			}
			if (exchange.getRequest().getMethod() != HttpMethod.POST) {
				return false;
			}
			String path = exchange.getRequest().getPath().pathWithinApplication().value();
			log.info("auth_matcher: {}, matcherpath1: {}, matcherpath2: {}", path, matchPathes[0],
					matchPathes[1]);
			return Stream.of(matchPathes).anyMatch(path::equals);
		}).switchIfEmpty(MatchResult.notMatch());
	}
}
