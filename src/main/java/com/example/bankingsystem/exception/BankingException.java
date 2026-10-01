package com.example.bankingsystem.exception;

/**
 * Thrown when a banking rule is violated (insufficient funds, frozen account,
 * invalid amount, etc.). The message is a user-friendly explanation.
 */
public class BankingException extends RuntimeException {

    public BankingException(String message) {
        super(message);
    }
}