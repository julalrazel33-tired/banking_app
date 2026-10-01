package com.example.bankingsystem.service;

import com.example.bankingsystem.dto.LoanForm;
import com.example.bankingsystem.dto.LoanPaymentForm;
import com.example.bankingsystem.entity.Account;
import com.example.bankingsystem.entity.AccountStatus;
import com.example.bankingsystem.entity.Loan;
import com.example.bankingsystem.entity.LoanStatus;
import com.example.bankingsystem.entity.TransactionType;
import com.example.bankingsystem.exception.BankingException;
import com.example.bankingsystem.exception.ResourceNotFoundException;
import com.example.bankingsystem.repository.AccountRepository;
import com.example.bankingsystem.repository.CustomerRepository;
import com.example.bankingsystem.repository.LoanRepository;
import com.example.bankingsystem.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LoanService {

    private final LoanRepository loanRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public LoanService(LoanRepository loanRepository,
                       CustomerRepository customerRepository,
                       AccountRepository accountRepository,
                       TransactionRepository transactionRepository) {
        this.loanRepository = loanRepository;
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public List<Loan> list() {
        return loanRepository.findAllByOrderByDateCreatedDesc();
    }

    public Loan get(Long id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));
    }

    public List<Loan> loansForCustomer(Long customerId) {
        return loanRepository.findByCustomerIdOrderByDateCreatedDesc(customerId);
    }

    @Transactional
    public Loan create(LoanForm form) {
        validatePositiveAmount(form.getPrincipalAmount(), "Loan amount must be greater than zero");
        var customer = customerRepository.findById(form.getCustomerId())
                .orElseThrow(() -> new BankingException("Selected customer does not exist"));

        Loan loan = new Loan();
        loan.setCustomer(customer);
        loan.setLoanType(form.getLoanType());
        loan.setPrincipalAmount(form.getPrincipalAmount());
        loan.setInterestRate(form.getInterestRate());
        loan.setRemainingBalance(form.getPrincipalAmount());
        loan.setStatus(LoanStatus.ACTIVE);
        return loanRepository.save(loan);
    }

    /**
     * Accepts a payment for a loan. The money is taken from an active account
     * that belongs to the loan's customer and the remaining balance is reduced.
     * The remaining balance is never allowed to become negative and a payment
     * larger than the remaining balance is rejected.
     */
    @Transactional
    public Loan makePayment(Long loanId, LoanPaymentForm form) {
        Loan loan = get(loanId);
        Account account = accountRepository.findById(form.getAccountId())
                .orElseThrow(() -> new BankingException("Payment account does not exist"));

        if (!loan.getCustomer().getId().equals(account.getCustomer().getId())) {
            throw new BankingException("Payment account must belong to the loan's customer");
        }
        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new BankingException("This loan has already been paid off");
        }

        BigDecimal amount = form.getAmount();
        validatePositiveAmount(amount, "Payment amount must be greater than zero");
        if (amount.compareTo(loan.getRemainingBalance()) > 0) {
            throw new BankingException("Payment of " + amount + " exceeds the remaining balance of "
                    + loan.getRemainingBalance());
        }
        if (account.getStatus() == AccountStatus.FROZEN) {
            throw new BankingException("Payment account " + account.getAccountNumber() + " is frozen");
        }
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new BankingException("Payment account " + account.getAccountNumber() + " is closed");
        }
        if (account.getBalance().compareTo(amount) < 0) {
            throw new BankingException("Insufficient funds in payment account " + account.getAccountNumber());
        }

        account.debit(amount);
        loan.setRemainingBalance(loan.getRemainingBalance().subtract(amount));
        if (loan.getRemainingBalance().compareTo(BigDecimal.ZERO) == 0) {
            loan.setStatus(LoanStatus.PAID);
            loan.setDatePaidOff(LocalDateTime.now());
        }

        accountRepository.save(account);
        loanRepository.save(loan);
        transactionRepository.save(TransactionFactory.of(
                TransactionType.LOAN_PAYMENT, amount, account, null,
                "Loan payment on loan #" + loan.getId()));
        return loan;
    }

    private void validatePositiveAmount(BigDecimal amount, String message) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankingException(message);
        }
    }
}