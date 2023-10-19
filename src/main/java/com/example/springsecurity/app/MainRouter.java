package com.example.springsecurity.app;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class MainRouter {

	@Bean
	RouterFunction<ServerResponse> htmxweb(HtmxwebService webService, PlaygroundService playgroundService) {
		return RouterFunctions.route().path("/",
				b -> {
					b.GET("/", (ServerRequest req) -> webService.index(req));
					b.GET("/custom-login-page", (ServerRequest req) -> webService.loginGet(req));
					b.GET("/custom-access-deny-page",
							(ServerRequest req) -> webService.accessdenedpage(req));
					b.GET("/filters", req -> webService.listWebfilterBeans(req));
					b.path("/playground", b1 -> {
						b1.GET("", playgroundService::home);
						b1.GET("/hxheaders", playgroundService::hxheaders);
					});
					b.GET("/language-switcher", playgroundService::langswitch);
					b.path("/protected", b1 -> {
						b1.path("/adminonly", b2 -> {
							b2.GET("/a", webService::protectedadmin);
						});
						b1.GET("/a", (ServerRequest req) -> webService.protecteda(req));
						b1.GET("/{*tpl}", (ServerRequest req) -> {
							String tpl = req.pathVariable("tpl");
							return webService.tpl(req, tpl);
						});
					});
				}).build();
	}

}
