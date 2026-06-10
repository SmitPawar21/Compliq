package com.smit.compliq.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.*;

import com.smit.compliq.dto.RegisterDTO;
import com.smit.compliq.entity.User;
import com.smit.compliq.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {
	
	private final UserService userService;
	
	public UserController(UserService userService) {
		super();
		this.userService = userService;
	}

	@GetMapping("/")
	public ResponseEntity<?> getAllUsers() {
		try {
			List<User> users = userService.getAllUsers();
			if(users.isEmpty()) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No User Found");
			}
			
			Map<String, Object> responseMap = new HashMap<>();
			responseMap.put("userCount", users.size());
			responseMap.put("users", users);
			
			return ResponseEntity.status(HttpStatus.CREATED).body(responseMap);
			
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}
//	GET    /api/users/{id}
	@GetMapping("/{id}")
	public ResponseEntity<?> getOneUser(@PathVariable long id) {
		try {
			User user = userService.getOneUser(id);
			if(user==null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No User Found");
			}
			
			return ResponseEntity.status(HttpStatus.CREATED).body(user);
			
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<?> putOneUser(@PathVariable long id, @Valid @RequestBody RegisterDTO userDto) {
		try {
	        User updatedUser = userService.updateUser(id, userDto);
	        
	        if (updatedUser == null) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No User Found");
	        }
	        
	        return ResponseEntity.ok(updatedUser);
	        
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
	    }
	}
	
//	DELETE /api/users/{id}
	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteOneUser(@PathVariable long id) {
		try {
			User deletedUser = userService.deleteUser(id);
			
			if (deletedUser == null) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No User Found");
	        }
	        
	        return ResponseEntity.ok(deletedUser);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}
}
