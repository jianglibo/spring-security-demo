package com.example.springsecurity.sec;

import org.springframework.security.authentication.AbstractAuthenticationToken;

import com.example.springsecurity.data.User;

public class FinalAuthenticatedToken extends AbstractAuthenticationToken {

	private User principal;

	public FinalAuthenticatedToken(User principal) {
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
