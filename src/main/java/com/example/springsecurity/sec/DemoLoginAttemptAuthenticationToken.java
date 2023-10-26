package com.example.springsecurity.sec;

import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DemoLoginAttemptAuthenticationToken extends AbstractAuthenticationToken {
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
