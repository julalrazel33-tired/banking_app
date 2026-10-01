package com.example.bankingsystem.service;

import com.example.bankingsystem.dto.AccountForm;
import com.example.bankingsystem.entity.Account;
import com.example.bankingsystem.entity.AccountStatus;
import com.example.bankingsystem.entity.TransactionType;
import com.example.bankingsystem.exception.BankingException;
import com.example.bankingsystem.exception.ResourceNotFoundException;
import com.example.bankingsystem.repository.AccountRepository;
import com.example.bankingsystem.repository.CustomerRepository;
import com.example.bankingsystem.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;

    public AccountService(AccountRepository accountRepository,
                          CustomerRepository customerRepository,
                          TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
    }

    public List<Account> list() {
        return accountRepository.findAllByOrderByDateCreatedDesc();
    }

    public Account get(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
    }

    public Account getByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account '" + accountNumber + "' not found"));
    }

    public List<Account> accountsForCustomer(Long customerId) {
        return accountRepository.findByCustomerId(customerId);
    }

    /**
     * Opens a new account for a customer. Account numbers are unique digits.
     */
    @Transactional
    public Account create(AccountForm form) {
        var customer = customerRepository.findById(form.getCustomerId())
                .orElseThrow(() -> new BankingException("Selected customer does not exist"));

        Account account = new Account();
        account.setCustomer(customer);
        account.setAccountType(form.getAccountType());
        account.setBalance(form.getOpeningBalance());
        account.setStatus(AccountStatus.ACTIVE);
        account.setAccountNumber(generateAccountNumber());
        account = accountRepository.save(account);

        if (form.getOpeningBalance().compareTo(BigDecimal.ZERO) > 0) {
            transactionRepository.save(TransactionFactory.of(
                    TransactionType.DEPOSIT, form.getOpeningBalance(), account, null,
                    "Initial deposit when opening account " + account.getAccountNumber()));
        }
        return account;
    }

    @Transactional
    public void freeze(Long id) {
        Account account = get(id);
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new BankingException("Cannot freeze a closed account");
        }
        if (account.getStatus() == AccountStatus.FROZEN) {
            throw new BankingException("Account " + account.getAccountNumber() + " is already frozen");
        }
        account.setStatus(AccountStatus.FROZEN);
        accountRepository.save(account);
    }

    @Transactional
    public void unfreeze(Long id) {
        Account account = get(id);
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new BankingException("Cannot unfreeze a closed account");
        }
        if (account.getStatus() == AccountStatus.ACTIVE) {
            throw new BankingException("Account " + account.getAccountNumber() + " is not frozen");
        }
        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);
    }

    private String generateAccountNumber() {
        String number;
        do {
            number = String.format("%010d", ThreadLocalRandom.current().nextInt(1_000_000_000));
        } while (accountRepository.existsByAccountNumber(number));
        return number;
    }
}