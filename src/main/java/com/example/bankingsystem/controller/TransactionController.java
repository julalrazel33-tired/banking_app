package com.example.bankingsystem.controller;

import com.example.bankingsystem.service.BankingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/transactions")
public class TransactionController {

    private final BankingService bankingService;

    public TransactionController(BankingService bankingService) {
        this.bankingService = bankingService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("transactions", bankingService.allTransactions());
        return "admin/transactions/list";
    }
}