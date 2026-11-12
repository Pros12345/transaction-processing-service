
package com.assessment.transaction.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionProcessingScheduler {

	private final TransactionService transactionService;

	@Scheduled(fixedDelay = 2000)
	public void processTransactions() {

		log.info("Background transaction processing started");

		transactionService.prpZEAWYtiB6bJ16NuLbGCc6CZ6jJdKfb63();

		log.info("Background transaction processing completed");
	}
}
