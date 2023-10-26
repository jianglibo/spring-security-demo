package com.example.springsecurity.sec;

import java.nio.charset.Charset;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class IfNeededCsrfMissing implements ServerAccessDeniedHandler {

	@Override
	public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException denied) {
		return Mono.just(exchange.getResponse())
				.flatMap((response) -> {
					response.setStatusCode(HttpStatus.FORBIDDEN);
					response.getHeaders()
							.setContentType(MediaType.TEXT_PLAIN);
					String msg = String.format("csrf stopped access, method: %s, path: %s",
							exchange.getRequest().getMethod(), exchange.getRequest().getPath());
					log.error(msg, denied);
					DataBufferFactory dataBufferFactory = response
							.bufferFactory();
					DataBuffer buffer = dataBufferFactory
							.wrap(denied.getMessage().getBytes(Charset
									.defaultCharset()));
					return response.writeWith(Mono.just(buffer))
							.doOnError((error) -> DataBufferUtils
									.release(buffer));
				});
	}

}
