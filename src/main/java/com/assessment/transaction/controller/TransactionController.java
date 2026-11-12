
package com.assessment.transaction.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.assessment.transaction.dto.TransactionRequest;
import com.assessment.transaction.dto.TransactionResponse;
import com.assessment.transaction.enums.TransactionStatus;
import com.assessment.transaction.service.TransactionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

	private final TransactionService transactionService;

	@PostMapping
	public ResponseEntity<TransactionResponse> createTransaction(@Valid @RequestBody TransactionRequest request) {

		TransactionResponse response = transactionService.receiveTransaction(request);

		if (response.getStatus() == TransactionStatus.DUPLICATE) {
			return ResponseEntity.ok(response);
		}

		return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
	}

	@GetMapping("/summary")
	public ResponseEntity<Map<String, Long>> getProcessingSummary() {

		return ResponseEntity.ok(transactionService.getProcessingSummary());
	}
}
