package com.example.springsecurity.app;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseCookie;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.example.springsecurity.data.UserRepo;
import com.example.springsecurity.htmx.HxRequestHeaders;
import com.example.springsecurity.htmx.HxResponseHeaders;
import com.example.springsecurity.htmx.HxResponseUtil;
import com.example.springsecurity.htmx.ThymeleafCtxFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class PlaygroundService {

	@Autowired
	HxResponseUtil hxResponseUtil;

	@Autowired
	ServerRequestCache webSessionServerRequestCache;

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	SecurityWebFilterChain securityWebFilterChain;

	@Autowired
	Environment environment;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	UserRepo userRepo;

	@Autowired
	ThymeleafCtxFactory thymeleafCtxFactory;

	public Mono<ServerResponse> home(ServerRequest req) {
		return thymeleafCtxFactory.create(req, null, false).flatMap(ctx -> {
			return ServerResponse.ok().render("playground", ctx.getModel());
		});
	}

	public Mono<ServerResponse> hxheaders(ServerRequest req) {
		String action = req.queryParam("action").orElse("none");
		return thymeleafCtxFactory.create(req, null, false).flatMap(ctx -> {
			switch (action) {
				case "show":
					List<Map<String, String>> headers = HxResponseHeaders.toList();
					return ServerResponse.ok().render("hxheaders/index :: hx-response-headers",
							ctx.getModel(Map.of("headers", headers, "selected", headers.get(0))));
				case "hide":
					return ServerResponse.ok().render("hxheaders/index :: hx-response-headers",
							ctx.getModel(Map.of()));
				case "headname-changed":
					String headname = req.queryParam("headname").orElse(null);
					Assert.notNull(headname, "headname must not be null");
					headers = HxResponseHeaders.toList();
					return ServerResponse.ok().render("hxheaders/index :: hx-response-headers",
							ctx.getModel(Map.of("headers", headers, "selected", headers.stream().filter(m -> {
								return headname.equals(m.get("name"));
							}).findFirst().orElse(headers.get(0)))));
				default:
					break;
			}
			return Mono.justOrEmpty(req.queryParam("headname"))
					.flatMap(headname -> {
						switch (HxResponseHeaders.fromValue(headname)) {
							case TRIGGER:
								return ServerResponse.noContent()
										.header(HxResponseHeaders.TRIGGER.getValue(),
												req.queryParam("headvalue").orElse(headname))
										.build();
							default:
								break;
						}
						return hxResponseUtil.r204();
					});
		});
	}

	public Mono<ServerResponse> langswitch(ServerRequest req) {
		return req.exchange().getSession().flatMap(ws -> {
			String language = req.queryParam("lang").orElse("en");
			ws.getAttributes().put("lang", language);
			log.debug("language switcher session id: {}", ws.getId());
			req.exchange().getResponse().addCookie(ResponseCookie.from(
					"lang", language)
					.path("/")
					.sameSite("Strict")
					.httpOnly(true)
					.maxAge(java.time.Duration.ofDays(365))
					.build());

			return Mono.defer(() -> {
				if (HxRequestHeaders.isHxRequest(req)) {
					String referer = req.headers().firstHeader(HxRequestHeaders.CURRENT_URL.getValue());
					// referer = getNewPath(language, referer);
					return hxResponseUtil.redirectTo(referer == null ? "/" : referer);
				} else {
					String referer = req.queryParam("referer").orElse("/");
					// referer = getNewPath(language, referer);
					return ResponseUtil.found(URI.create(referer))
							.build();
				}
			});
		});

	}
}
