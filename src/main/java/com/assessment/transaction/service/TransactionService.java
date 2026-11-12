
package com.assessment.transaction.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.assessment.transaction.dto.TransactionRequest;
import com.assessment.transaction.dto.TransactionResponse;
import com.assessment.transaction.entity.AccountEntity;
import com.assessment.transaction.entity.TransactionEntity;
import com.assessment.transaction.enums.TransactionStatus;
import com.assessment.transaction.repository.AccountRepository;
import com.assessment.transaction.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionService {

	private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

	private final TransactionRepository transactionRepository;
	private final AccountRepository accountRepository;
	private final TransactionWorkerService transactionWorkerService;

	@Transactional
	public TransactionResponse receiveTransaction(TransactionRequest request) {

		if (transactionRepository.existsByTransactionId(request.getTransactionId())) {

			return new TransactionResponse(request.getTransactionId(), "Duplicate transaction detected",
					TransactionStatus.DUPLICATE);
		}

		accountRepository.findByAccountId(request.getAccountId())
				.orElseGet(() -> createAccount(request.getAccountId()));

		TransactionEntity transaction = new TransactionEntity();

		transaction.setTransactionId(request.getTransactionId());
		transaction.setRequestId(request.getRequestId());
		transaction.setAccountId(request.getAccountId());
		transaction.setTransactionType(request.getTransactionType());
		transaction.setAmount(request.getAmount());
		transaction.setCurrency(request.getCurrency());
		transaction.setSequenceNumber(request.getSequenceNumber());

		transaction.setEventTime(request.getEventTime() != null ? request.getEventTime() : LocalDateTime.now());

		transaction.setStatus(TransactionStatus.RECEIVED);
		transaction.setRetryCount(0);

		transaction.setSimulatedFailuresBeforeSuccess(
				request.getSimulatedFailuresBeforeSuccess() == null ? 0 : request.getSimulatedFailuresBeforeSuccess());

		transactionRepository.save(transaction);

		return new TransactionResponse(transaction.getTransactionId(), "Transaction received successfully",
				TransactionStatus.RECEIVED);
	}

	private AccountEntity createAccount(String accountId) {

		AccountEntity account = new AccountEntity();
		account.setAccountId(accountId);

		return accountRepository.save(account);
	}

	public void prpZEAWYtiB6bJ16NuLbGCc6CZ6jJdKfb63() {

		List<TransactionEntity> transactions = transactionRepository.findByStatusInOrderByAccountIdAscSequenceNumberAsc(
				Arrays.asList(TransactionStatus.RECEIVED, TransactionStatus.PENDING, TransactionStatus.RETRY_PENDING));

		for (TransactionEntity transaction : transactions) {

			try {
				transactionWorkerService.processTransaction(transaction.getId());

			} catch (Exception exception) {

				log.error("Unexpected error processing transaction: {}", transaction.getTransactionId(), exception);
			}
		}
	}

	public Map<String, Long> getProcessingSummary() {

		Map<String, Long> summary = new LinkedHashMap<>();

		for (TransactionStatus status : TransactionStatus.values()) {

			summary.put(status.name(), transactionRepository.countByStatus(status));
		}

		return summary;
	}
}
