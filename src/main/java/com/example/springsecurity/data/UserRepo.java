package com.example.springsecurity.data;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.Getter;

@Getter
public class UserRepo {

	private List<UserInDb> users;

	public UserInDb byName(String name) {
		return users.stream().filter(u -> u.getName().equals(name)).findFirst().orElse(null);
	}

	public UserInDb byAccessKey(String accessKey) {
		return users.stream().filter(u -> u.getAccesskeys().contains(accessKey)).findFirst().orElse(null);
	}

	public void init(PasswordEncoder passwordEncoder) {
		users = List.of(
				UserInDb.builder().name("admin").password(passwordEncoder.encode("admin"))
						.roles(List.of(Role.builder().name("ROLE_ADMIN").build()))
						.accesskeys(List.of("ieReituNg3do4jiruquaiGaiJaexauna")).build(),

				UserInDb.builder().name("user").password(passwordEncoder.encode("user"))
						.roles(List.of(Role.builder().name("ROLE_USER").build()))
						.accesskeys(List.of("heixoo7ahC3eh7pielaitoh6lood9coo")).build()

		);
	}

}
