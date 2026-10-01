package com.example.bankingsystem.controller;

import com.example.bankingsystem.dto.AccountForm;
import com.example.bankingsystem.dto.MoneyForm;
import com.example.bankingsystem.entity.Account;
import com.example.bankingsystem.exception.BankingException;
import com.example.bankingsystem.service.AccountService;
import com.example.bankingsystem.service.BankingService;
import com.example.bankingsystem.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/accounts")
public class AccountController {

    private final AccountService accountService;
    private final CustomerService customerService;
    private final BankingService bankingService;

    public AccountController(AccountService accountService,
                             CustomerService customerService,
                             BankingService bankingService) {
        this.accountService = accountService;
        this.customerService = customerService;
        this.bankingService = bankingService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("accounts", accountService.list());
        return "admin/accounts/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        if (!model.containsAttribute("accountForm")) {
            model.addAttribute("accountForm", new AccountForm());
        }
        model.addAttribute("customers", customerService.list());
        return "admin/accounts/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("accountForm") AccountForm form,
                         BindingResult bindingResult, Model model, RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("customers", customerService.list());
            return "admin/accounts/form";
        }
        try {
            Account account = accountService.create(form);
            ra.addFlashAttribute("success",
                    "Account " + account.getAccountNumber() + " opened successfully");
            return "redirect:/admin/accounts/" + account.getId();
        } catch (BankingException e) {
            model.addAttribute("customers", customerService.list());
            model.addAttribute("error", e.getMessage());
            return "admin/accounts/form";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Account account = accountService.get(id);
        model.addAttribute("account", account);
        model.addAttribute("transactions", bankingService.historyFor(account));
        model.addAttribute("moneyForm", new MoneyForm());
        return "admin/accounts/detail";
    }

    @PostMapping("/{id}/deposit")
    public String deposit(@PathVariable Long id,
                          @Valid @ModelAttribute MoneyForm moneyForm,
                          BindingResult bindingResult, RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            ra.addFlashAttribute("error", firstFieldErrorMessage(bindingResult, "Enter a valid amount greater than zero"));
            return "redirect:/admin/accounts/" + id;
        }
        try {
            bankingService.deposit(id, moneyForm.getAmount());
            ra.addFlashAttribute("success", "Deposit completed successfully");
        } catch (BankingException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/accounts/" + id;
    }

    @PostMapping("/{id}/withdraw")
    public String withdraw(@PathVariable Long id,
                           @Valid @ModelAttribute MoneyForm moneyForm,
                           BindingResult bindingResult, RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            ra.addFlashAttribute("error", firstFieldErrorMessage(bindingResult, "Enter a valid amount greater than zero"));
            return "redirect:/admin/accounts/" + id;
        }
        try {
            bankingService.withdraw(id, moneyForm.getAmount());
            ra.addFlashAttribute("success", "Withdrawal completed successfully");
        } catch (BankingException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/accounts/" + id;
    }

    @PostMapping("/{id}/freeze")
    public String freeze(@PathVariable Long id, RedirectAttributes ra) {
        try {
            accountService.freeze(id);
            ra.addFlashAttribute("success", "Account frozen successfully");
        } catch (BankingException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/accounts/" + id;
    }

    @PostMapping("/{id}/unfreeze")
    public String unfreeze(@PathVariable Long id, RedirectAttributes ra) {
        try {
            accountService.unfreeze(id);
            ra.addFlashAttribute("success", "Account unfrozen successfully");
        } catch (BankingException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/accounts/" + id;
    }

    private String firstFieldErrorMessage(BindingResult bindingResult, String fallback) {
        return bindingResult.getFieldErrors().stream()
                .findFirst()
                .map(fieldError -> fieldError.getDefaultMessage())
                .orElse(fallback);
    }
}