package com.example.springsecurity.app;

import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;

import com.example.springsecurity.data.User;
import com.example.springsecurity.data.UserInDb;
import com.example.springsecurity.data.UserRepo;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import reactor.core.publisher.Mono;

public class DemoAuthorizationReactiveAuthenticationManager implements ReactiveAuthenticationManager {
	private final UserRepo userRepo;
	private final PasswordEncoder passwordEncoder;

	public DemoAuthorizationReactiveAuthenticationManager(UserRepo userRepo, PasswordEncoder passwordEncoder) {
		this.userRepo = userRepo;
		this.passwordEncoder = passwordEncoder;
	}

	/**
	 * Don't need do any thing to authenticate, because the existence of the headers
	 * {@link me.resp.sb3.demo.configprops.CustomHttpHeaders} tells all.
	 */
	@Override
	public Mono<Authentication> authenticate(Authentication authentication) {
		if (authentication instanceof DemoLoginAttemptAuthenticationToken) {
			DemoLoginAttemptAuthenticationToken callbackToken = (DemoLoginAttemptAuthenticationToken) authentication;
			UserInDb userInDb;
			String presentedPassword = (String) callbackToken.getCredentials();
			if (callbackToken.isXapi()) {
				userInDb = userRepo.byAccessKey(presentedPassword);
			} else {
				userInDb = userRepo.byName(callbackToken.getName());
				if (userInDb != null) {
					if (!passwordEncoder.matches(presentedPassword, userInDb.getPassword())) {
						userInDb = null;
					}
				}
			}
			if (userInDb == null) {
				return Mono.error(new BadCredentialsException("Invalid Credentials"));
			} else {
				return Mono.just(new User( //
						userInDb.getName(), //
						userInDb.getRoles() //
				)).map(user -> new DemoAuthAuthenticationToken(user));
			}

		} else {
			throw new IllegalArgumentException("unkown authencation token type.");
		}
	}

	public static class DemoAuthAuthenticationToken extends AbstractAuthenticationToken {

		private User principal;

		public DemoAuthAuthenticationToken(User principal) {
			super(principal.getAuthorities());
			super.eraseCredentials();
			this.principal = principal;
			this.setAuthenticated(true);
		}

		@Override
		public Object getCredentials() {
			return null;
		}

		@Override
		public User getPrincipal() {
			return this.principal;
		}

	}

	public static class DemoLoginAttemptAuthenticationToken extends AbstractAuthenticationToken {

		@Getter
		private boolean xapi;

		private String name;
		private String secret;

		public DemoLoginAttemptAuthenticationToken(Collection<? extends GrantedAuthority> authorities) {
			super(authorities);
		}

		public DemoLoginAttemptAuthenticationToken(String username, String password) {
			super(null);
			this.name = username;
			this.secret = password;
			this.xapi = false;
		}

		public DemoLoginAttemptAuthenticationToken(String xapi) {
			super(null);
			this.secret = xapi;
			this.xapi = true;
		}

		@Override
		public Object getCredentials() {
			return secret;
		}

		@Override
		public Object getPrincipal() {
			return name;
		}
	}

	public static class DemoServerAuthenticationConverter implements ServerAuthenticationConverter {
		private final ObjectMapper objectMapper;

		/**
		 * @param objectMapper
		 */
		public DemoServerAuthenticationConverter(ObjectMapper objectMapper) {
			this.objectMapper = objectMapper;
		}

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
}
