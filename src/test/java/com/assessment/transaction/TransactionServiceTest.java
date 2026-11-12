
package com.assessment.transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.assessment.transaction.dto.TransactionRequest;
import com.assessment.transaction.dto.TransactionResponse;
import com.assessment.transaction.entity.AccountEntity;
import com.assessment.transaction.entity.TransactionEntity;
import com.assessment.transaction.enums.TransactionStatus;
import com.assessment.transaction.enums.TransactionType;
import com.assessment.transaction.repository.AccountRepository;
import com.assessment.transaction.repository.TransactionRepository;
import com.assessment.transaction.service.TransactionService;
import com.assessment.transaction.service.TransactionWorkerService;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

	@Mock
	private TransactionRepository transactionRepository;

	@Mock
	private AccountRepository accountRepository;

	@Mock
	private TransactionWorkerService transactionWorkerService;

	@InjectMocks
	private TransactionService transactionService;

	@Test
	void shouldReturnDuplicateWhenTransactionAlreadyExists() {

		TransactionRequest request = createRequest();

		when(transactionRepository.existsByTransactionId("TXN-TEST-001")).thenReturn(true);

		TransactionResponse response = transactionService.receiveTransaction(request);

		assertEquals(TransactionStatus.DUPLICATE, response.getStatus());

		verify(transactionRepository, never()).save(any(TransactionEntity.class));
	}

	@Test
	void shouldReceiveNewTransactionSuccessfully() {

		TransactionRequest request = createRequest();

		when(transactionRepository.existsByTransactionId("TXN-TEST-001")).thenReturn(false);

		when(accountRepository.findByAccountId("ACC-TEST-001")).thenReturn(Optional.empty());

		when(accountRepository.save(any(AccountEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

		when(transactionRepository.save(any(TransactionEntity.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		TransactionResponse response = transactionService.receiveTransaction(request);

		assertEquals(TransactionStatus.RECEIVED, response.getStatus());

		verify(accountRepository).save(any(AccountEntity.class));
		verify(transactionRepository).save(any(TransactionEntity.class));
	}

	private TransactionRequest createRequest() {

		TransactionRequest request = new TransactionRequest();

		request.setTransactionId("TXN-TEST-001");
		request.setRequestId("REQ-TEST-001");
		request.setAccountId("ACC-TEST-001");
		request.setTransactionType(TransactionType.CREDIT);
		request.setAmount(new BigDecimal("500.00"));
		request.setCurrency("INR");
		request.setSequenceNumber(1L);
		request.setSimulatedFailuresBeforeSuccess(0);

		return request;
	}
}
