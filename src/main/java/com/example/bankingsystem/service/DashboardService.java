package com.example.bankingsystem.service;

import com.example.bankingsystem.dto.DashboardStats;
import com.example.bankingsystem.entity.AccountStatus;
import com.example.bankingsystem.entity.LoanStatus;
import com.example.bankingsystem.repository.AccountRepository;
import com.example.bankingsystem.repository.CustomerRepository;
import com.example.bankingsystem.repository.LoanRepository;
import com.example.bankingsystem.repository.TransactionRepository;
import org.springframework.stereotype.Service;

/**
 * Aggregates the figures shown on the admin dashboard. Everything comes from
 * the database; no figures are fabricated.
 */
@Service
public class DashboardService {

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final LoanRepository loanRepository;

    public DashboardService(CustomerRepository customerRepository,
                            AccountRepository accountRepository,
                            TransactionRepository transactionRepository,
                            LoanRepository loanRepository) {
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.loanRepository = loanRepository;
    }

    public DashboardStats stats() {
        DashboardStats stats = new DashboardStats();
        stats.setTotalCustomers(customerRepository.count());
        stats.setTotalAccounts(accountRepository.count());
        stats.setTotalBalance(accountRepository.sumTotalBalance());
        stats.setRecentTransactions(transactionRepository.findTop10ByOrderByTransactionDateDesc());
        stats.setActiveLoans(loanRepository.countByStatus(LoanStatus.ACTIVE));
        stats.setFrozenAccounts(accountRepository.countByStatus(AccountStatus.FROZEN));
        return stats;
    }
}