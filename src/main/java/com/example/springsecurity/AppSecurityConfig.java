package com.example.springsecurity;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.ServerAuthenticationEntryPointFailureHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.logout.RedirectServerLogoutSuccessHandler;
import org.springframework.security.web.server.authentication.logout.SecurityContextServerLogoutHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.security.web.server.authorization.ServerWebExchangeDelegatingServerAccessDeniedHandler;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.security.web.server.csrf.ServerCsrfTokenRepository;
import org.springframework.security.web.server.csrf.WebSessionServerCsrfTokenRepository;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import org.springframework.security.web.server.savedrequest.WebSessionServerRequestCache;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;

import com.example.springsecurity.app.AppProperties;
import com.example.springsecurity.app.DemoAuthenticationFilter;
import com.example.springsecurity.app.DemoAuthorizationReactiveAuthenticationManager;
import com.example.springsecurity.data.UserRepo;
import com.example.springsecurity.htmx.HxRequestHeaders;
import com.example.springsecurity.htmx.HxResponseHeaders;
import com.example.springsecurity.htmx.HxResponseUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Configuration
public class AppSecurityConfig {

	@Bean
	PasswordEncoder passwordEncoder() {
		int strength = 10; // work factor of bcrypt
		return new BCryptPasswordEncoder(strength, new SecureRandom());
	}

	@Bean
	UserRepo userRepo(PasswordEncoder passwordEncoder) {
		UserRepo userRepo = new UserRepo();
		userRepo.init(passwordEncoder);
		return userRepo;
	}

	@Bean
	// new WebSessionServerCsrfTokenRepository();
	ServerCsrfTokenRepository serverCsrfTokenRepository() {
		return new WebSessionServerCsrfTokenRepository();
	}

	@Bean
	// new DefaultRequireCsrfProtectionMatcher();
	ServerWebExchangeMatcher csrfProtectionMatcher() {
		return new ServerWebExchangeMatcher() {
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
			};
		};
	}

	// @Bean
	// WebSessionIdResolver webSessionIdResolver() {
	// CookieWebSessionIdResolver resolver = new CookieWebSessionIdResolver();
	// resolver.setCookieName("JSESSIONID");
	// resolver.addCookieInitializer((builder) -> builder.path("/"));
	// resolver.addCookieInitializer((builder) -> builder.sameSite("Strict"));
	// return resolver;
	// }

	@Bean
	ServerRequestCache serverRequestCache() {
		return new WebSessionServerRequestCache();
	}

	@Bean
	// csrf header name or parameter name could be configured here.
	ServerSecurityContextRepository serverSecurityContextRepository() {
		return new WebSessionServerSecurityContextRepository();
	}

	@Bean
	ServerLogoutSuccessHandler serverLogoutSuccessHandler() {
		RedirectServerLogoutSuccessHandler handler = new RedirectServerLogoutSuccessHandler();
		handler.setLogoutSuccessUrl(URI.create("/"));
		return handler;
	}

	@Bean
	ServerAuthenticationEntryPoint entryPoint(AppProperties appProperties,
			ServerRequestCache serverRequestCache) {
		RedirectServerAuthenticationEntryPoint entryPoint = new RedirectServerAuthenticationEntryPoint(
				"/custom-login-page");
		entryPoint.setRequestCache(serverRequestCache);
		return entryPoint;
	}

	@Bean
	@Order(Ordered.HIGHEST_PRECEDENCE)
	SecurityWebFilterChain simpleAuthWebFilterChain(
			ServerHttpSecurity http,
			AppProperties appProperties,
			ObjectMapper objectMapper,
			ServerAuthenticationSuccessHandler serverAuthenticationSuccessHandler,
			ServerAuthenticationFailureHandler serverAuthenticationFailureHandler,
			ServerWebExchangeMatcher csrfProtectionMatcher,
			ReactiveAuthenticationManager authenticationManager,
			ServerSecurityContextRepository serverSecurityContextRepository,
			ServerAuthenticationEntryPoint entryPoint,
			ServerRequestCache serverRequestCache) {
		http.formLogin(fl -> fl.disable());
		http.httpBasic(hb -> hb.disable());

		http.csrf(cc -> cc
				.requireCsrfProtectionMatcher(csrfProtectionMatcher)
				.csrfTokenRepository(serverCsrfTokenRepository())
				.accessDeniedHandler((exchange, ex) -> {
					return Mono.defer(() -> Mono.just(exchange.getResponse()))
							.flatMap((response) -> {
								response.setStatusCode(HttpStatus.FORBIDDEN);
								response.getHeaders()
										.setContentType(MediaType.TEXT_PLAIN);
								String msg = String.format("csrf stopped access, method: %s, path: %s",
										exchange.getRequest().getMethod(), exchange.getRequest().getPath());
								log.error(msg, ex);
								DataBufferFactory dataBufferFactory = response
										.bufferFactory();
								DataBuffer buffer = dataBufferFactory
										.wrap(ex.getMessage().getBytes(Charset
												.defaultCharset()));
								return response.writeWith(Mono.just(buffer))
										.doOnError((error) -> DataBufferUtils
												.release(buffer));
							});
				}));
		http.cors(Customizer.withDefaults());

		WebFilter demoAuthenticationFilter = demoAuthenticationFilter(
				serverAuthenticationSuccessHandler,
				serverAuthenticationFailureHandler,
				appProperties,
				objectMapper, authenticationManager,
				serverSecurityContextRepository);

		http.addFilterAt(demoAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION);

		http.requestCache(rcc -> rcc.requestCache(serverRequestCache));
		http.securityMatcher(
				ServerWebExchangeMatchers.pathMatchers("/**"))
				.authorizeExchange((exchanges) -> exchanges
						.pathMatchers("/protected/adminonly/**").hasAnyRole("ADMIN")
						.pathMatchers("/protected/**")
						.authenticated()
						.anyExchange()
						.permitAll()
						.and()
						.logout(logout -> logout
								.requiresLogout(ServerWebExchangeMatchers.pathMatchers(HttpMethod.GET, "/leave"))
								.logoutHandler(
										new SecurityContextServerLogoutHandler())
								.logoutSuccessHandler(serverLogoutSuccessHandler()))
						.exceptionHandling(exh -> exh.authenticationEntryPoint(entryPoint)
								.accessDeniedHandler(serverAccessDeniedHandler())));
		return http.build();
	}

	@Bean
	ServerAccessDeniedHandler serverAccessDeniedHandler() {
		ServerWebExchangeDelegatingServerAccessDeniedHandler.DelegateEntry entry = new ServerWebExchangeDelegatingServerAccessDeniedHandler.DelegateEntry(
				new ServerWebExchangeMatcher() {
					@Override
					public Mono<MatchResult> matches(ServerWebExchange exchange) {
						return MatchResult.match();
					}
				}, /* new HttpStatusServerAccessDeniedHandler(HttpStatus.FORBIDDEN) */
				new ServerAccessDeniedHandler() {
					@Override
					public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException denied) {
						// redirecto /custom-access-deny-page
						return Mono.defer(() -> {
							ServerHttpResponse response = exchange.getResponse();
							response.setStatusCode(HttpStatus.SEE_OTHER);
							URI newLocation = URI
									.create("/custom-access-deny-page?url=" + exchange.getRequest().getPath().value());
							response.getHeaders().setLocation(newLocation);
							return Mono.empty();
						});
					}
				});
		ServerWebExchangeDelegatingServerAccessDeniedHandler handler = new ServerWebExchangeDelegatingServerAccessDeniedHandler(
				List.of(entry));
		return handler;
	}

	@Bean
	ServerAuthenticationFailureHandler serverAuthenticationFailureHandler(ObjectMapper objectMapper) {
		return new ServerAuthenticationEntryPointFailureHandler(
				(exchange, ex) -> {
					return Mono.defer(() -> {
						// if the Accept header is application/json return json
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
						String uri = "/custom-login-page?error="
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
					});
				});
	}

	@Bean
	// Different login method need different success handler
	ServerAuthenticationSuccessHandler serverAuthenticationSuccessHandler(HxResponseUtil hxResponseUtil,
			ServerRequestCache serverRequestCache) {
		return (webFilterExchange, authentication) -> {
			return Mono.defer(() -> {
				ServerWebExchange exchange = webFilterExchange.getExchange();
				return serverRequestCache.getRedirectUri(exchange)
						.defaultIfEmpty(URI.create("/"))
						.flatMap(uri -> {
							ServerHttpResponse response = exchange.getResponse();
							if (HxRequestHeaders.isHxRequest(exchange.getRequest())) {
								String reqPath = exchange.getRequest().getPath().value();
								if (reqPath.startsWith("/custom-login-page")) {
									response.getHeaders().add(HxResponseHeaders.REDIRECT.getValue(), uri.toString());
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
			});
		};
	}

	@Bean
	ReactiveAuthenticationManager demoAuthenticationManager(UserRepo userRepo,
			PasswordEncoder passwordEncoder) {
		return new DemoAuthorizationReactiveAuthenticationManager(userRepo, passwordEncoder);
	}

	public WebFilter demoAuthenticationFilter(
			ServerAuthenticationSuccessHandler serverAuthenticationSuccessHandler,
			ServerAuthenticationFailureHandler serverAuthenticationFailureHandler,
			AppProperties appProperties,
			ObjectMapper objectMapper,
			ReactiveAuthenticationManager demoAuthorizationReactiveAuthenticationManager,
			ServerSecurityContextRepository serverSecurityContextRepository) {
		DemoAuthenticationFilter filter = new DemoAuthenticationFilter(
				demoAuthorizationReactiveAuthenticationManager,
				serverAuthenticationSuccessHandler,
				serverAuthenticationFailureHandler,
				appProperties, objectMapper);
		filter.setSecurityContextRepository(serverSecurityContextRepository); //
		return filter;
	}

}
