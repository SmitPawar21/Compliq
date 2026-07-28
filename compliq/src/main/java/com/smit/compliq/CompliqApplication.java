package com.smit.compliq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class CompliqApplication {

	public static void main(String[] args) {
		SpringApplication.run(CompliqApplication.class, args);
	}	

}
