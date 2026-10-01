package com.example.bankingsystem.exception;

/**
 * Thrown when a requested resource (customer, account, loan, admin) does not
 * exist so controllers can respond with a friendly 404 page.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}