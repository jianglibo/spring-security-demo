package com.example.springsecurity.data;

import java.util.Collection;

import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.GrantedAuthority;

import lombok.Getter;

public class User implements AuthenticatedPrincipal {

	@Getter
	private final Collection<GrantedAuthority> authorities;

	private final String name;

	public User(String name, Collection<Role> roles) {
		this.name = name;
		this.authorities =  roles.stream().map(r -> (GrantedAuthority) r).toList();
	}

	@Override
	public String getName() {
		return name;	
	}

}
