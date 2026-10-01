package com.example.bankingsystem.service;

import com.example.bankingsystem.entity.Account;
import com.example.bankingsystem.entity.Transaction;
import com.example.bankingsystem.entity.TransactionType;

import java.math.BigDecimal;

/**
 * Small helper for building transaction records consistently across services.
 */
public final class TransactionFactory {

    private TransactionFactory() {
    }

    public static Transaction of(TransactionType type, BigDecimal amount,
                                 Account source, Account destination, String description) {
        Transaction transaction = new Transaction();
        transaction.setTransactionType(type);
        transaction.setAmount(amount);
        transaction.setSourceAccount(source);
        transaction.setDestinationAccount(destination);
        transaction.setDescription(description);
        return transaction;
    }
}