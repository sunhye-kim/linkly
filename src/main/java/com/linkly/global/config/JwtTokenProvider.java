package com.linkly.global.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

	private final JwtProperties jwtProperties;

	// JWT 토큰 생성
	public String generateToken(Long userId, String email) {
		Date now = new Date();
		Date expiryDate = new Date(now.getTime() + jwtProperties.getExpiration());

		return Jwts.builder()
				.setSubject(userId.toString())
				.claim("email", email)
				.setIssuedAt(now)
				.setExpiration(expiryDate)
				.signWith(getSigningKey(), SignatureAlgorithm.HS256)
				.compact();
	}

	// JWT 토큰에서 사용자 ID 추출
	public Long getUserIdFromToken(String token) {
		Claims claims = parseToken(token);
		return Long.parseLong(claims.getSubject());
	}

	// JWT 토큰에서 이메일 추출
	public String getEmailFromToken(String token) {
		Claims claims = parseToken(token);
		return claims.get("email", String.class);
	}

	// JWT 토큰 유효성 검증
	public boolean validateToken(String token) {
		try {
			parseToken(token);
			return true;
		} catch (SecurityException | MalformedJwtException e) {
			log.error("Invalid JWT signature: {}", e.getMessage());
		} catch (ExpiredJwtException e) {
			log.error("Expired JWT token: {}", e.getMessage());
		} catch (UnsupportedJwtException e) {
			log.error("Unsupported JWT token: {}", e.getMessage());
		} catch (IllegalArgumentException e) {
			log.error("JWT claims string is empty: {}", e.getMessage());
		}
		return false;
	}

	// JWT 토큰 파싱
	private Claims parseToken(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(getSigningKey())
				.build()
				.parseClaimsJws(token)
				.getBody();
	}

	// 서명 키 생성
	private Key getSigningKey() {
		byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
		return Keys.hmacShaKeyFor(keyBytes);
	}
}