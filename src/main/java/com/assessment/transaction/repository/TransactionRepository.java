
package com.assessment.transaction.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.assessment.transaction.entity.TransactionEntity;
import com.assessment.transaction.enums.TransactionStatus;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {

	boolean existsByTransactionId(String transactionId);

	Optional<TransactionEntity> findByTransactionId(String transactionId);

	List<TransactionEntity> findByAccountId(String accountId);

	Optional<TransactionEntity> findByAccountIdAndSequenceNumber(String accountId, Long sequenceNumber);

	List<TransactionEntity> findByStatus(TransactionStatus status);

	List<TransactionEntity> findByAccountIdAndStatusOrderBySequenceNumberAsc(String accountId,
			TransactionStatus status);

	List<TransactionEntity> findByStatusInOrderByAccountIdAscSequenceNumberAsc(Collection<TransactionStatus> statuses);

	long countByStatus(TransactionStatus status);
}
