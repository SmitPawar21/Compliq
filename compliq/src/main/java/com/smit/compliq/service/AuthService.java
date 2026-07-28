package com.smit.compliq.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.smit.compliq.dto.RegisterDTO;
import com.smit.compliq.entity.User;
import com.smit.compliq.entity.Organization;
import com.smit.compliq.repository.UserRepository;
import com.smit.compliq.repository.OrganizationRepository;
import com.smit.compliq.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class AuthService {
	private final UserRepository userRepo;
	private final PasswordEncoder passwordEncoder;
	private final OrganizationRepository orgRepo;
	private final OrganizationService orgService;
	
	public AuthService(UserRepository userRepo, PasswordEncoder passwordEncoder, OrganizationRepository orgRepo, OrganizationService orgService) {
		this.userRepo = userRepo;
		this.passwordEncoder = passwordEncoder;
		this.orgRepo = orgRepo;
		this.orgService = orgService;
	}
	
	@Transactional
	public User registerNewUser(RegisterDTO request) {
		if(userRepo.existsByUsername(request.getUsername())) {
			throw new RuntimeException("username already exists");
		}
		
		if(userRepo.existsByEmail(request.getEmail())) {
			throw new RuntimeException("email already exists");
		}
		
		try {
			User user = new User();
			user.setUsername(request.getUsername());
			user.setEmail(request.getEmail());
			user.setPassword(passwordEncoder.encode(request.getPassword()));
			user.setRole(request.getRole());
			user.setCreatedAt(request.getCreatedAt());
			
			Organization org = orgRepo.findByName(request.getOrganizationName())
				.orElseGet(() -> {
					Organization newOrg = new Organization();
					newOrg.setName(request.getOrganizationName());
					newOrg.setSubscription(com.smit.compliq.enums.SubscriptionType.BASIC);
					return orgService.createOrganization(newOrg);
				});
			user.setOrganization(org);
			
			System.out.println("in user service: "+user);
			System.out.println("role: "+request.getRole());
			return userRepo.save(user);
		} catch (Exception e) {
			System.out.println("Error in userService. "+e);
			return null;
		}
	}
}