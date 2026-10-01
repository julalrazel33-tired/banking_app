package com.example.bankingsystem.entity;

/**
 * The outcome of a transaction. Only successful operations are persisted.
 */
public enum TransactionStatus {
    SUCCESS,
    FAILED
}