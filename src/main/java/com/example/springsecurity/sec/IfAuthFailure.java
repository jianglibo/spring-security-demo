package com.example.springsecurity.sec;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationEntryPointFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.example.springsecurity.htmx.HxRequestHeaders;
import com.example.springsecurity.htmx.HxResponseHeaders;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class IfAuthFailure extends ServerAuthenticationEntryPointFailureHandler {

	@Autowired
	ObjectMapper objectMapper;

	public IfAuthFailure(ServerAuthenticationEntryPoint authenticationEntryPoint) {
		super(authenticationEntryPoint);
	}

	@Override
	public Mono<Void> onAuthenticationFailure(WebFilterExchange webFilterExchange, AuthenticationException ex) {
		ServerWebExchange exchange = webFilterExchange.getExchange();

		if (exchange.getRequest().getHeaders().getAccept().contains(MediaType.APPLICATION_JSON)) {
			ServerHttpResponse response = exchange.getResponse();
			response.setStatusCode(HttpStatus.OK);
			response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
			Map<String, Object> errorMsg = Map.of("error", ex.getMessage());
			DataBufferFactory dataBufferFactory = response.bufferFactory();
			return Mono
					.fromCallable(
							() -> dataBufferFactory.wrap(objectMapper.writeValueAsBytes(errorMsg)))
					.flatMap(buffer -> {
						return response.writeWith(Mono.just(buffer))
								.doOnError((error) -> DataBufferUtils.release(buffer));
					});
		}
		String uri = exchange.getRequest().getPath().contextPath().value() + "/custom-login-page?error="
				+ URLEncoder.encode(ex.getMessage(), StandardCharsets.UTF_8);
		ServerHttpResponse response = exchange.getResponse();
		if (HxRequestHeaders.isHxRequest(exchange.getRequest())) {
			response.getHeaders().add(HxResponseHeaders.REDIRECT.getValue(),
					uri);
			response.setStatusCode(HttpStatus.NO_CONTENT);
		} else {
			response.setStatusCode(HttpStatus.SEE_OTHER);
			response.getHeaders().setLocation(URI.create(uri));
		}
		return Mono.empty();
	}

}
