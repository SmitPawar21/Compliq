package com.smit.compliq.service;

import org.springframework.stereotype.Service;

import com.smit.compliq.dto.RegisterDTO;
import com.smit.compliq.entity.User;
import com.smit.compliq.repository.UserRepository;

import java.util.*;

@Service
public class UserService {
	private UserRepository userRepository;

	public List<User> getAllUsers() {
		return userRepository.findAll();
	}
	
	public User getOneUser(long user_id) {
		return userRepository.findById(user_id);
	}
	
	public User updateUser(long user_id, RegisterDTO dto) {
		User user = userRepository.findById(user_id);
		if(user==null) return null;
		
		user.setUsername(dto.getUsername());
		user.setEmail(dto.getEmail());
		user.setPassword(dto.getPassword());
		user.setRole(dto.getRole());	
		
		return userRepository.save(user);
	}
	
	public User deleteUser(long user_id) {
		User user = userRepository.findById(user_id);
		if(user==null) return null;
		
		userRepository.delete(user);
		return user;
	}
}
