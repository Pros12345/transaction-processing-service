package com.assessment.transaction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class TransactionProcessingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TransactionProcessingServiceApplication.class, args);
	}

}
