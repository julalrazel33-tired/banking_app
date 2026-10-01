package com.example.bankingsystem.service;

import com.example.bankingsystem.dto.TransferForm;
import com.example.bankingsystem.entity.Account;
import com.example.bankingsystem.entity.AccountStatus;
import com.example.bankingsystem.entity.Transaction;
import com.example.bankingsystem.entity.TransactionType;
import com.example.bankingsystem.exception.BankingException;
import com.example.bankingsystem.exception.ResourceNotFoundException;
import com.example.bankingsystem.repository.AccountRepository;
import com.example.bankingsystem.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Core banking operations. All methods that change money are transactional so
 * a failure can never leave the database partially updated.
 */
@Service
public class BankingService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public BankingService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public Account deposit(Long accountId, BigDecimal amount) {
        validatePositiveAmount(amount);
        Account account = requireAccount(accountId);
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new BankingException("Cannot deposit into a closed account");
        }
        account.credit(amount);
        accountRepository.save(account);
        transactionRepository.save(TransactionFactory.of(
                TransactionType.DEPOSIT, amount, account, null,
                "Deposit to account " + account.getAccountNumber()));
        return account;
    }

    @Transactional
    public Account withdraw(Long accountId, BigDecimal amount) {
        validatePositiveAmount(amount);
        Account account = requireAccount(accountId);
        requireActiveAccount(account);
        if (account.getBalance().compareTo(amount) < 0) {
            throw new BankingException("Insufficient funds. Available balance is " + displayAmount(account.getBalance()));
        }
        account.debit(amount);
        accountRepository.save(account);
        transactionRepository.save(TransactionFactory.of(
                TransactionType.WITHDRAWAL, amount, account, null,
                "Withdrawal from account " + account.getAccountNumber()));
        return account;
    }

    @Transactional
    public void transfer(TransferForm form) {
        validatePositiveAmount(form.getAmount());

        String sourceNumber = form.getSourceAccountNumber().trim();
        String destinationNumber = form.getDestinationAccountNumber().trim();

        if (sourceNumber.equals(destinationNumber)) {
            throw new BankingException("Source and destination accounts must be different");
        }

        Account source = accountRepository.findByAccountNumber(sourceNumber)
                .orElseThrow(() -> new BankingException("Source account '" + sourceNumber + "' does not exist"));
        Account destination = accountRepository.findByAccountNumber(destinationNumber)
                .orElseThrow(() -> new BankingException("Destination account '" + destinationNumber + "' does not exist"));

        requireAccountActiveForTransfer("Source", source);
        requireAccountActiveForTransfer("Destination", destination);

        if (source.getBalance().compareTo(form.getAmount()) < 0) {
            throw new BankingException(
                    "Insufficient funds in account " + source.getAccountNumber() + ". Available balance is "
                            + displayAmount(source.getBalance()));
        }

        source.debit(form.getAmount());
        destination.credit(form.getAmount());
        accountRepository.save(source);
        accountRepository.save(destination);

        transactionRepository.save(TransactionFactory.of(
                TransactionType.TRANSFER, form.getAmount(), source, destination,
                "Transfer from " + sourceNumber + " to " + destinationNumber));
    }

    public List<Transaction> historyFor(Account account) {
        return transactionRepository
                .findBySourceAccount_IdOrDestinationAccount_IdOrderByTransactionDateDesc(account.getId(), account.getId());
    }

    public List<Transaction> allTransactions() {
        return transactionRepository.findAllByOrderByTransactionDateDesc();
    }

    private Account requireAccount(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
    }

    private void requireActiveAccount(Account account) {
        if (account.getStatus() == AccountStatus.FROZEN) {
            throw new BankingException("Account " + account.getAccountNumber() + " is frozen, withdrawals are not allowed");
        }
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new BankingException("Cannot operate on closed account " + account.getAccountNumber());
        }
    }

    private void requireAccountActiveForTransfer(String label, Account account) {
        if (account.getStatus() == AccountStatus.FROZEN) {
            throw new BankingException(label + " account " + account.getAccountNumber() + " is frozen");
        }
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new BankingException(label + " account " + account.getAccountNumber() + " is closed");
        }
    }

    private void validatePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankingException("Amount must be greater than zero");
        }
    }

    private String displayAmount(BigDecimal amount) {
        return "$" + amount.setScale(2, java.math.RoundingMode.HALF_UP);
    }
}