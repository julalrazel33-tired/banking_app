package com.example.bankingsystem.controller;

import com.example.bankingsystem.dto.TransferForm;
import com.example.bankingsystem.exception.BankingException;
import com.example.bankingsystem.service.AccountService;
import com.example.bankingsystem.service.BankingService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/transfers")
public class TransferController {

    private final BankingService bankingService;
    private final AccountService accountService;

    public TransferController(BankingService bankingService, AccountService accountService) {
        this.bankingService = bankingService;
        this.accountService = accountService;
    }

    @GetMapping
    public String form(Model model) {
        if (!model.containsAttribute("transferForm")) {
            model.addAttribute("transferForm", new TransferForm());
        }
        model.addAttribute("accounts", accountService.list());
        return "admin/transfers/form";
    }

    @PostMapping
    public String transfer(@Valid @ModelAttribute("transferForm") TransferForm form,
                           BindingResult bindingResult, Model model, RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("accounts", accountService.list());
            return "admin/transfers/form";
        }
        try {
            bankingService.transfer(form);
            ra.addFlashAttribute("success",
                    "Transfer of $" + form.getAmount() + " from " + form.getSourceAccountNumber()
                            + " to " + form.getDestinationAccountNumber() + " completed successfully");
            return "redirect:/admin/transfers";
        } catch (BankingException e) {
            model.addAttribute("accounts", accountService.list());
            model.addAttribute("error", e.getMessage());
            return "admin/transfers/form";
        }
    }
}