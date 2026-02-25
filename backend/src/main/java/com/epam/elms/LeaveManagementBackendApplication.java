package com.epam.elms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.epam.elms"})
public class LeaveManagementBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(LeaveManagementBackendApplication.class, args);
	}

}
