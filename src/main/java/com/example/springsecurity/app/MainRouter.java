package com.example.springsecurity.app;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class MainRouter {

	@Bean
	RouterFunction<ServerResponse> htmxweb(HtmxwebService webService) {
		return route().path("/",
				b -> {
					b.GET("/", (ServerRequest req) -> webService.index(req));
					b.GET("/custom-login-page", (ServerRequest req) -> webService.loginGet(req));
					b.GET("/custom-access-deny-page",
							(ServerRequest req) -> webService.accessdenedpage(req));
					// b.POST("/custom-login-page", (ServerRequest req) ->
					// webService.loginPost(req));
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
					// b.GET("/{*tpl}", (ServerRequest req) -> webService.tpl(req));
				}).build();
	}

}
