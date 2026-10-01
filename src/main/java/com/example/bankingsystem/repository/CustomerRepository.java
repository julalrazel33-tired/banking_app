package com.example.bankingsystem.repository;

import com.example.bankingsystem.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);

    List<Customer> findAllByOrderByDateCreatedDesc();
}