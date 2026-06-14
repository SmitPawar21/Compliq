package com.smit.compliq.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smit.compliq.dto.JwtResponseDTO;
import com.smit.compliq.dto.LoginDTO;
import com.smit.compliq.dto.RegisterDTO;
import com.smit.compliq.entity.User;
import com.smit.compliq.security.JwtUtil;
import com.smit.compliq.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
	private AuthService authService;
	private AuthenticationManager authManager;
	private JwtUtil jwtUtil;
	
	public AuthController(AuthService authService, AuthenticationManager authManager,
			JwtUtil jwtUtil) {
		this.authService = authService;
		this.authManager = authManager;
		this.jwtUtil = jwtUtil;
	}
	
	@PostMapping("/register")
	public ResponseEntity<?> register(@Valid @RequestBody RegisterDTO request) {
		try {
			User user = authService.registerNewUser(request);
			return ResponseEntity.status(HttpStatus.CREATED).body("User Registered Successfully"+ user);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error"+e);
		}
	}
	
	@PostMapping("/login")
	public ResponseEntity<?> login(@Valid @RequestBody LoginDTO request) {
		try {
			Authentication authentication = authManager.authenticate(
					new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
				);
			
			UserDetails principle = (UserDetails) authentication.getPrincipal();
			String jwt = jwtUtil.generateToken(principle.getUsername());	
			
			return ResponseEntity.status(HttpStatus.CREATED).body(new JwtResponseDTO(jwt, principle.getUsername()));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e);
		}
	}
}
