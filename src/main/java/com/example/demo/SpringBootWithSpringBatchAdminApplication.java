package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration;

@SpringBootApplication(exclude = FreeMarkerAutoConfiguration.class)
public class SpringBootWithSpringBatchAdminApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootWithSpringBatchAdminApplication.class, args);
	}

}

