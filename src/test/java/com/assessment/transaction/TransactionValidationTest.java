
package com.assessment.transaction;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.assessment.transaction.dto.TransactionRequest;
import com.assessment.transaction.enums.TransactionType;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class TransactionValidationTest {

	private static ValidatorFactory validatorFactory;

	private static Validator validator;

	@BeforeAll
	static void setUp() {

		validatorFactory = Validation.buildDefaultValidatorFactory();

		validator = validatorFactory.getValidator();
	}

	@AfterAll
	static void tearDown() {

		validatorFactory.close();
	}

	@Test
	void shouldRejectNegativeTransactionAmount() {

		TransactionRequest request = createValidRequest();

		request.setAmount(new BigDecimal("-100.00"));

		Set<ConstraintViolation<TransactionRequest>> violations = validator.validate(request);

		assertFalse(violations.isEmpty());
	}

	@Test
	void shouldRejectMissingTransactionId() {

		TransactionRequest request = createValidRequest();

		request.setTransactionId("");

		Set<ConstraintViolation<TransactionRequest>> violations = validator.validate(request);

		assertTrue(violations.stream()
				.anyMatch(violation -> violation.getPropertyPath().toString().equals("transactionId")));
	}

	@Test
	void shouldAcceptValidTransactionRequest() {

		TransactionRequest request = createValidRequest();

		Set<ConstraintViolation<TransactionRequest>> violations = validator.validate(request);

		assertTrue(violations.isEmpty());
	}

	private TransactionRequest createValidRequest() {

		TransactionRequest request = new TransactionRequest();

		request.setTransactionId("TXN-VALID-001");
		request.setRequestId("REQ-VALID-001");
		request.setAccountId("ACC-VALID-001");
		request.setTransactionType(TransactionType.CREDIT);
		request.setAmount(new BigDecimal("500.00"));
		request.setCurrency("INR");
		request.setSequenceNumber(1L);

		return request;
	}
}
