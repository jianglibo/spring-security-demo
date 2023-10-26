package com.example.springsecurity.sec;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.springsecurity.data.User;
import com.example.springsecurity.data.UserInDb;
import com.example.springsecurity.data.UserRepo;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class DemoAuthorizationReactiveAuthenticationManager implements ReactiveAuthenticationManager {
	private final UserRepo userRepo;
	private final PasswordEncoder passwordEncoder;

	public DemoAuthorizationReactiveAuthenticationManager(
			UserRepo userRepo,
			PasswordEncoder passwordEncoder) {
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
				)).map(user -> new FinalAuthenticatedToken(user));
			}

		} else {
			throw new IllegalArgumentException("unkown authencation token type.");
		}
	}
}
