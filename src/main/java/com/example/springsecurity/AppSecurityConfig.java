package com.example.springsecurity;

import java.security.SecureRandom;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import org.springframework.security.web.server.authentication.logout.SecurityContextServerLogoutHandler;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.security.web.server.csrf.ServerCsrfTokenRepository;
import org.springframework.security.web.server.csrf.WebSessionServerCsrfTokenRepository;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import org.springframework.security.web.server.savedrequest.WebSessionServerRequestCache;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.server.WebFilter;

import com.example.springsecurity.app.AppProperties;
import com.example.springsecurity.data.UserRepo;
import com.example.springsecurity.sec.AfterAuthenticateSuccess;
import com.example.springsecurity.sec.AfterLogoutSuccessfully;
import com.example.springsecurity.sec.DemoAuthenticationFilter;
import com.example.springsecurity.sec.DemoAuthorizationReactiveAuthenticationManager;
import com.example.springsecurity.sec.ExtractAuthenticateInfoFromExchange;
import com.example.springsecurity.sec.IfAccessDenied;
import com.example.springsecurity.sec.IfAuthFailure;
import com.example.springsecurity.sec.IfNeededCsrfMissing;
import com.example.springsecurity.sec.IsItAauthenticationRequest;
import com.example.springsecurity.sec.WhenNeedAuthentication;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

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
	ServerRequestCache serverRequestCache() {
		return new WebSessionServerRequestCache();
	}

	@Bean
	// csrf header name or parameter name could be configured here.
	ServerSecurityContextRepository serverSecurityContextRepository() {
		return new WebSessionServerSecurityContextRepository();
	}

	@Bean
	@Order(Ordered.HIGHEST_PRECEDENCE)
	SecurityWebFilterChain simpleAuthWebFilterChain(
			ServerHttpSecurity http,
			AppProperties appProperties,
			ServerProperties serverProperties,
			ObjectMapper objectMapper,
			AfterAuthenticateSuccess afterAuthenticateSuccess,
			ServerAuthenticationFailureHandler serverAuthenticationFailureHandler,
			ServerWebExchangeMatcher csrfProtectionMatcher,
			DemoAuthorizationReactiveAuthenticationManager authenticationManager,
			ServerSecurityContextRepository serverSecurityContextRepository,
			WhenNeedAuthentication WhenNeedAuthentication,
			IfAccessDenied ifAccessDenied,
			IfNeededCsrfMissing ifNeededCsrfMissing,
			AfterLogoutSuccessfully afterLogoutSuccessfully,
			IsItAauthenticationRequest isItAauthenticationRequest,
			ExtractAuthenticateInfoFromExchange extractAuthenticateInfoFromExchange,
			IfAuthFailure ifAuthFailure,
			ServerRequestCache serverRequestCache,
			@Value("${spring.webflux.base-path:}") String contextPath) {
		http.formLogin(fl -> fl.disable());
		http.httpBasic(hb -> hb.disable());

		http.csrf(cc -> cc
				.requireCsrfProtectionMatcher(csrfProtectionMatcher)
				.csrfTokenRepository(serverCsrfTokenRepository())
				.accessDeniedHandler(ifNeededCsrfMissing));
		http.cors(Customizer.withDefaults());

		WebFilter demoAuthenticationFilter = new DemoAuthenticationFilter(
				authenticationManager,
				afterAuthenticateSuccess,
				serverSecurityContextRepository,
				isItAauthenticationRequest,
				extractAuthenticateInfoFromExchange,
				ifAuthFailure,
				appProperties,
				objectMapper);

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
								.logoutSuccessHandler(afterLogoutSuccessfully))
						.exceptionHandling(exh -> exh.authenticationEntryPoint(WhenNeedAuthentication)
								.accessDeniedHandler(ifAccessDenied)));
		return http.build();
	}

}
