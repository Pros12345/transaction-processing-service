
package com.assessment.transaction.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.assessment.transaction.enums.TransactionType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionRequest {

	@NotBlank(message = "Transaction ID is required")
	private String transactionId;

	@NotBlank(message = "Request ID is required")
	private String requestId;

	@NotBlank(message = "Account ID is required")
	private String accountId;

	@NotNull(message = "Transaction type is required")
	private TransactionType transactionType;

	@NotNull(message = "Amount is required")
	@DecimalMin(value = "0.01", message = "Amount must be greater than zero")
	private BigDecimal amount;

	@NotBlank(message = "Currency is required")
	private String currency;

	@NotNull(message = "Sequence number is required")
	@Positive(message = "Sequence must be positive")
	private Long sequenceNumber;

	private LocalDateTime eventTime;

	@Min(value = 0, message = "Simulated failures cannot be negative")
	@Max(value = 3, message = "Maximum simulated failures allowed is 3")
	private Integer simulatedFailuresBeforeSuccess = 0;
}
