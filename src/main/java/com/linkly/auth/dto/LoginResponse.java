package com.linkly.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

	private String accessToken;
	private String tokenType = "Bearer";
	private Long userId;
	private String email;
	private String name;

	public LoginResponse(String accessToken, Long userId, String email, String name) {
		this.accessToken = accessToken;
		this.tokenType = "Bearer";
		this.userId = userId;
		this.email = email;
		this.name = name;
	}
}
