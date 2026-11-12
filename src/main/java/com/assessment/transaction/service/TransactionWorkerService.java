
package com.assessment.transaction.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.assessment.transaction.entity.AccountEntity;
import com.assessment.transaction.entity.TransactionEntity;
import com.assessment.transaction.enums.TransactionStatus;
import com.assessment.transaction.repository.AccountRepository;
import com.assessment.transaction.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionWorkerService {

	private static final int MAX_RETRIES = 3;

	private final TransactionRepository transactionRepository;
	private final AccountRepository accountRepository;

	@Transactional
	public void processTransaction(Long transactionId) {

		TransactionEntity transaction = transactionRepository.findById(transactionId).orElse(null);

		if (transaction == null) {
			log.warn("Transaction {} not found", transactionId);
			return;
		}

		if (transaction.getStatus() == TransactionStatus.PROCESSED
				|| transaction.getStatus() == TransactionStatus.FAILED) {
			return;
		}

		AccountEntity account = accountRepository.findByAccountId(transaction.getAccountId()).orElse(null);

		if (account == null) {
			markFailed(transaction, "Account not found");
			return;
		}

		long expectedSequence = account.getLastProcessedSequence() + 1;

		if (transaction.getSequenceNumber() > expectedSequence) {
			transaction.setStatus(TransactionStatus.PENDING);
			transaction.setFailureReason("Waiting for sequence number " + expectedSequence);
			transactionRepository.save(transaction);
			return;
		}

		if (transaction.getSequenceNumber() < expectedSequence) {
			markFailed(transaction, "Stale or already processed sequence");
			return;
		}

		transaction.setStatus(TransactionStatus.PROCESSING);
		transactionRepository.save(transaction);

		// Simulate transient failures before successful processing.
		if (transaction.getRetryCount() < transaction.getSimulatedFailuresBeforeSuccess()) {

			transaction.setRetryCount(transaction.getRetryCount() + 1);

			if (transaction.getRetryCount() >= MAX_RETRIES) {
				markFailed(transaction, "Maximum retry attempts exhausted");
			} else {
				transaction.setStatus(TransactionStatus.RETRY_PENDING);
				transaction.setFailureReason("Simulated transient failure");

				log.warn("Transaction {} failed temporarily. Retry {}/{}", transaction.getTransactionId(),
						transaction.getRetryCount(), MAX_RETRIES);
			}

			transactionRepository.save(transaction);
			return;
		}

		BigDecimal currentBalance = account.getBalance();
		BigDecimal amount = transaction.getAmount();

		switch (transaction.getTransactionType()) {

		case CREDIT:
			account.setBalance(currentBalance.add(amount));
			break;

		case DEBIT:
			if (currentBalance.compareTo(amount) < 0) {
				markFailed(transaction, "Insufficient balance");
				return;
			}

			account.setBalance(currentBalance.subtract(amount));
			break;

		case REVERSAL:
			markFailed(transaction, "Reversal requires an original transaction reference");
			return;

		default:
			markFailed(transaction, "Unsupported transaction type");
			return;
		}

		account.setLastProcessedSequence(transaction.getSequenceNumber());

		accountRepository.save(account);

		transaction.setStatus(TransactionStatus.PROCESSED);
		transaction.setFailureReason(null);

		transactionRepository.save(transaction);

		log.info("Transaction {} processed successfully. Account: {}, Balance: {}", transaction.getTransactionId(),
				account.getAccountId(), account.getBalance());
	}

	private void markFailed(TransactionEntity transaction, String reason) {

		transaction.setStatus(TransactionStatus.FAILED);
		transaction.setFailureReason(reason);

		transactionRepository.save(transaction);

		log.error("Transaction {} failed: {}", transaction.getTransactionId(), reason);
	}
}
