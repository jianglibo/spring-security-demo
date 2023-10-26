package com.example.springsecurity.sec;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class ExtractAuthenticateInfoFromExchange implements ServerAuthenticationConverter {

	@Override
	public Mono<Authentication> convert(ServerWebExchange exchange) {
		return Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst("X-API-KEY"))
				.map(xapi -> (Authentication) new DemoLoginAttemptAuthenticationToken(xapi))
				.switchIfEmpty(Mono.defer(() -> {
					return exchange.getFormData().map(formData -> {
						String username = formData.getFirst("username");
						String password = formData.getFirst("password");
						return new DemoLoginAttemptAuthenticationToken(username, password);
					});
				}));
	}
}
