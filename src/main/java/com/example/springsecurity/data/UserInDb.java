package com.example.springsecurity.data;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserInDb {
	private String name;
	private String password;
	@Builder.Default
	private List<String> accesskeys = new ArrayList<>();

	private List<Role> roles;
}
