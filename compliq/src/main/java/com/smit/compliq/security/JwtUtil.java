package com.smit.compliq.security;

import java.util.*;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
	@Value("${jwt.secret}")
	private String jwtSecret;
	
	@Value("${jwt.expiration}")
	private long expirationMs;
	
	private SecretKey getSigningKey() {
		return Keys.hmacShaKeyFor(jwtSecret.getBytes());
	}
	
	public String generateToken(String username) {
		Date now = new Date();
		Date expiryDate = new Date(now.getTime() + expirationMs);
		
		return Jwts.builder()
				.setSubject(username)
				.setIssuedAt(now)
				.setExpiration(expiryDate)
				.signWith(getSigningKey(), SignatureAlgorithm.HS512)
				.compact();
	}
	
	public String getUsernameFromJwt(String token) {
		Claims claims = Jwts.parserBuilder()
				.setSigningKey(getSigningKey())
				.build()
				.parseClaimsJws(token)
				.getBody();
		
		return claims.getSubject();
	}
	
	public boolean validateToken(String authToken) {
		try {
			Jwts.parserBuilder()
				.setSigningKey(getSigningKey())
				.build()
				.parseClaimsJws(authToken);
			
			return true;
		} catch (Exception e) {
			System.out.println(e);
			return false;
		}
	}
}
