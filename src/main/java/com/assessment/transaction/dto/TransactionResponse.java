
package com.assessment.transaction.dto;

import com.assessment.transaction.enums.TransactionStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

	private String transactionId;

	private String message;

	private TransactionStatus status;
}
