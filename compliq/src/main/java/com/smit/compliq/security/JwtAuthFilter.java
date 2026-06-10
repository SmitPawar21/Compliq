package com.smit.compliq.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.smit.compliq.service.CustomUserDetailService;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
	private final JwtUtil jwtUtil;
	private final CustomUserDetailService custUserService;
	
	public JwtAuthFilter(JwtUtil jwtUtil, CustomUserDetailService custUserService) {
		this.jwtUtil = jwtUtil;
		this.custUserService = custUserService;
	}
	
	private String getJwtFromRequest(HttpServletRequest request) {
		String bearer = request.getHeader("Authorization");
		if(StringUtils.hasText(bearer) && bearer.startsWith("Bearer")) {
			return bearer.substring(7);
		}
		
		return null;
	}
	
	@Override
	protected void doFilterInternal(HttpServletRequest request,
									HttpServletResponse response,
									FilterChain filterChain) throws ServletException, IOException {
		String token = getJwtFromRequest(request);
		
		if(StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
			String username = jwtUtil.getUsernameFromJwt(token);
			UserDetails userDetails = custUserService.loadUserByUsername(username);
			
			UsernamePasswordAuthenticationToken authentication = 
					new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
			
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}
		
		filterChain.doFilter(request, response);
	}
}
