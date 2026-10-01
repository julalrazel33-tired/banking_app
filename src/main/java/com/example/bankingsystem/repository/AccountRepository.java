package com.example.bankingsystem.repository;

import com.example.bankingsystem.entity.Account;
import com.example.bankingsystem.entity.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);

    boolean existsByAccountNumber(String accountNumber);

    long countByStatus(AccountStatus status);

    List<Account> findAllByOrderByDateCreatedDesc();

    List<Account> findByCustomerId(Long customerId);

    /**
     * Total balance held across all non-closed accounts.
     */
    @Query("select coalesce(sum(a.balance), 0) from Account a where a.status <> com.example.bankingsystem.entity.AccountStatus.CLOSED")
    BigDecimal sumTotalBalance();
}