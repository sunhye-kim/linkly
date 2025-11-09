package com.linkly.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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