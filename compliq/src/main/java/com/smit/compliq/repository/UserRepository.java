package com.smit.compliq.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smit.compliq.entity.User;
import com.smit.finDock.entity.UserEntity;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByUsername(String username); 
	User findById(long user_id);
	boolean existsByUsername(String username);
	boolean existsByEmail(String email);
}
