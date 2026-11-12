
package com.assessment.transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.assessment.transaction.entity.AccountEntity;
import com.assessment.transaction.entity.TransactionEntity;
import com.assessment.transaction.enums.TransactionStatus;
import com.assessment.transaction.enums.TransactionType;
import com.assessment.transaction.repository.AccountRepository;
import com.assessment.transaction.repository.TransactionRepository;
import com.assessment.transaction.service.TransactionWorkerService;

@ExtendWith(MockitoExtension.class)
class TransactionWorkerServiceTest {

	@Mock
	private TransactionRepository transactionRepository;

	@Mock
	private AccountRepository accountRepository;

	@InjectMocks
	private TransactionWorkerService workerService;

	private AccountEntity account;

	@BeforeEach
	void setUp() {

		account = new AccountEntity();
		account.setAccountId("ACC-TEST-001");
		account.setBalance(new BigDecimal("1000.00"));
		account.setLastProcessedSequence(0L);
	}

	@Test
	void shouldMarkTransactionPendingWhenSequenceIsMissing() {

		TransactionEntity transaction = createTransaction(2L, 0);

		when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

		when(accountRepository.findByAccountId("ACC-TEST-001")).thenReturn(Optional.of(account));

		workerService.processTransaction(1L);

		assertEquals(TransactionStatus.PENDING, transaction.getStatus());

		assertEquals(new BigDecimal("1000.00"), account.getBalance());

		verify(accountRepository, never()).save(any(AccountEntity.class));
	}

	@Test
	void shouldRetryAndProcessTransactionSuccessfully() {

		TransactionEntity transaction = createTransaction(1L, 1);

		when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

		when(accountRepository.findByAccountId("ACC-TEST-001")).thenReturn(Optional.of(account));

		// First attempt: simulated failure.
		workerService.processTransaction(1L);

		assertEquals(TransactionStatus.RETRY_PENDING, transaction.getStatus());

		assertEquals(1, transaction.getRetryCount());

		assertEquals(new BigDecimal("1000.00"), account.getBalance());

		// Second attempt: successful processing.
		workerService.processTransaction(1L);

		assertEquals(TransactionStatus.PROCESSED, transaction.getStatus());

		assertEquals(new BigDecimal("1500.00"), account.getBalance());

		assertEquals(1L, account.getLastProcessedSequence());

		verify(accountRepository).save(account);
	}

	@Test
	void shouldFailAfterMaximumRetryAttempts() {

		TransactionEntity transaction = createTransaction(1L, 3);

		when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

		when(accountRepository.findByAccountId("ACC-TEST-001")).thenReturn(Optional.of(account));

		// Three processing attempts.
		workerService.processTransaction(1L);
		workerService.processTransaction(1L);
		workerService.processTransaction(1L);

		assertEquals(TransactionStatus.FAILED, transaction.getStatus());

		assertEquals(3, transaction.getRetryCount());

		assertEquals("Maximum retry attempts exhausted", transaction.getFailureReason());

		// No credit should have been applied.
		assertEquals(new BigDecimal("1000.00"), account.getBalance());

		verify(accountRepository, never()).save(any(AccountEntity.class));
	}

	private TransactionEntity createTransaction(Long sequenceNumber, Integer simulatedFailures) {

		TransactionEntity transaction = new TransactionEntity();

		transaction.setId(1L);
		transaction.setTransactionId("TXN-TEST-001");
		transaction.setRequestId("REQ-TEST-001");
		transaction.setAccountId("ACC-TEST-001");
		transaction.setTransactionType(TransactionType.CREDIT);
		transaction.setAmount(new BigDecimal("500.00"));
		transaction.setCurrency("INR");
		transaction.setSequenceNumber(sequenceNumber);
		transaction.setStatus(TransactionStatus.RECEIVED);
		transaction.setRetryCount(0);
		transaction.setSimulatedFailuresBeforeSuccess(simulatedFailures);

		return transaction;
	}
}
