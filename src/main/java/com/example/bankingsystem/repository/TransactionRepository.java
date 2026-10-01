package com.example.bankingsystem.repository;

import com.example.bankingsystem.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findTop10ByOrderByTransactionDateDesc();

    List<Transaction> findAllByOrderByTransactionDateDesc();

    List<Transaction> findBySourceAccount_IdOrDestinationAccount_IdOrderByTransactionDateDesc(Long sourceId, Long destinationId);

    @Modifying
    @Query("delete from Transaction t where t.sourceAccount.id = :accountId or t.destinationAccount.id = :accountId")
    void deleteAllForAccountId(@Param("accountId") Long accountId);
}