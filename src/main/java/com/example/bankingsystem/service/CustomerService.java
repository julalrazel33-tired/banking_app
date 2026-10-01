package com.example.bankingsystem.service;

import com.example.bankingsystem.dto.CustomerForm;
import com.example.bankingsystem.entity.Account;
import com.example.bankingsystem.entity.Customer;
import com.example.bankingsystem.exception.BankingException;
import com.example.bankingsystem.exception.ResourceNotFoundException;
import com.example.bankingsystem.repository.AccountRepository;
import com.example.bankingsystem.repository.CustomerRepository;
import com.example.bankingsystem.repository.LoanRepository;
import com.example.bankingsystem.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final LoanRepository loanRepository;
    private final TransactionRepository transactionRepository;

    public CustomerService(CustomerRepository customerRepository,
                           AccountRepository accountRepository,
                           LoanRepository loanRepository,
                           TransactionRepository transactionRepository) {
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.loanRepository = loanRepository;
        this.transactionRepository = transactionRepository;
    }

    public List<Customer> list() {
        return customerRepository.findAllByOrderByDateCreatedDesc();
    }

    public Customer get(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    }

    @Transactional
    public Customer create(CustomerForm form) {
        if (customerRepository.existsByEmailIgnoreCase(form.getEmail())) {
            throw new BankingException("A customer with email '" + form.getEmail() + "' already exists");
        }
        Customer customer = new Customer();
        apply(form, customer);
        return customerRepository.save(customer);
    }

    @Transactional
    public Customer update(Long id, CustomerForm form) {
        Customer customer = get(id);
        customerRepository.findByEmail(form.getEmail())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new BankingException("A customer with email '" + form.getEmail() + "' already exists");
                });
        apply(form, customer);
        return customerRepository.save(customer);
    }

    /**
     * Removes the customer together with their accounts, loans and the
     * transaction history of those accounts so the database stays consistent.
     */
    @Transactional
    public void delete(Long id) {
        Customer customer = get(id);
        for (Account account : customer.getAccounts()) {
            transactionRepository.deleteAllForAccountId(account.getId());
        }
        accountRepository.deleteAll(customer.getAccounts());
        loanRepository.deleteAll(customer.getLoans());
        customerRepository.delete(customer);
    }

    private void apply(CustomerForm form, Customer customer) {
        customer.setFirstName(form.getFirstName().trim());
        customer.setLastName(form.getLastName().trim());
        customer.setEmail(form.getEmail().trim());
        customer.setPhone(form.getPhone() == null ? null : form.getPhone().trim());
        customer.setAddress(form.getAddress() == null ? null : form.getAddress().trim());
    }
}