package com.example.bankingsystem.service;

import com.example.bankingsystem.dto.AdminForm;
import com.example.bankingsystem.entity.Admin;
import com.example.bankingsystem.exception.BankingException;
import com.example.bankingsystem.exception.ResourceNotFoundException;
import com.example.bankingsystem.repository.AdminRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminService(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Admin> list() {
        return adminRepository.findAllByOrderByUsernameAsc();
    }

    public Admin get(Long id) {
        return adminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));
    }

    public Admin getByUsername(String username) {
        return adminRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));
    }

    public long count() {
        return adminRepository.count();
    }

    @Transactional
    public Admin create(AdminForm form) {
        if (adminRepository.existsByUsername(form.getUsername())) {
            throw new BankingException("Username '" + form.getUsername() + "' is already taken");
        }
        if (form.getPassword() == null || form.getPassword().isBlank()) {
            throw new BankingException("A password is required for a new admin");
        }
        Admin admin = new Admin();
        admin.setUsername(form.getUsername().trim());
        admin.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        admin.setRole(form.getRole());
        admin.setActive(form.isActive());
        return adminRepository.save(admin);
    }

    @Transactional
    public Admin update(Long id, AdminForm form) {
        Admin admin = get(id);
        adminRepository.findByUsername(form.getUsername())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new BankingException("Username '" + form.getUsername() + "' is already taken");
                });
        admin.setUsername(form.getUsername().trim());
        if (form.getPassword() != null && !form.getPassword().isBlank()) {
            admin.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        }
        admin.setRole(form.getRole());
        admin.setActive(form.isActive());
        return adminRepository.save(admin);
    }

    @Transactional
    public void delete(Long id, Long currentAdminId) {
        if (id.equals(currentAdminId)) {
            throw new BankingException("You cannot delete your own admin account");
        }
        adminRepository.deleteById(id);
    }
}