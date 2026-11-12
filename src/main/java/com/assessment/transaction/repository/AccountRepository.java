
package com.assessment.transaction.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.assessment.transaction.entity.AccountEntity;

public interface AccountRepository extends JpaRepository<AccountEntity, Long> {

	Optional<AccountEntity> findByAccountId(String accountId);

	boolean existsByAccountId(String accountId);

}
