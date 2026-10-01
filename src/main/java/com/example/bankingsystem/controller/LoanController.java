package com.example.bankingsystem.controller;

import com.example.bankingsystem.dto.LoanForm;
import com.example.bankingsystem.dto.LoanPaymentForm;
import com.example.bankingsystem.entity.Loan;
import com.example.bankingsystem.exception.BankingException;
import com.example.bankingsystem.service.AccountService;
import com.example.bankingsystem.service.CustomerService;
import com.example.bankingsystem.service.LoanService;
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
@RequestMapping("/admin/loans")
public class LoanController {

    private final LoanService loanService;
    private final CustomerService customerService;
    private final AccountService accountService;

    public LoanController(LoanService loanService,
                          CustomerService customerService,
                          AccountService accountService) {
        this.loanService = loanService;
        this.customerService = customerService;
        this.accountService = accountService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("loans", loanService.list());
        return "admin/loans/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        if (!model.containsAttribute("loanForm")) {
            model.addAttribute("loanForm", new LoanForm());
        }
        model.addAttribute("customers", customerService.list());
        return "admin/loans/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("loanForm") LoanForm form,
                         BindingResult bindingResult, Model model, RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("customers", customerService.list());
            return "admin/loans/form";
        }
        try {
            Loan loan = loanService.create(form);
            ra.addFlashAttribute("success", "Loan of $" + loan.getPrincipalAmount() + " granted to "
                    + loan.getCustomer().getFullName());
            return "redirect:/admin/loans/" + loan.getId();
        } catch (BankingException e) {
            model.addAttribute("customers", customerService.list());
            model.addAttribute("error", e.getMessage());
            return "admin/loans/form";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Loan loan = loanService.get(id);
        model.addAttribute("loan", loan);
        model.addAttribute("accounts", accountService.accountsForCustomer(loan.getCustomer().getId()));
        model.addAttribute("loanPaymentForm", new LoanPaymentForm());
        return "admin/loans/detail";
    }

    @PostMapping("/{id}/payment")
    public String payment(@PathVariable Long id,
                          @Valid @ModelAttribute("loanPaymentForm") LoanPaymentForm form,
                          BindingResult bindingResult, Model model, RedirectAttributes ra) {
        Loan loan = loanService.get(id);
        if (bindingResult.hasErrors()) {
            model.addAttribute("loan", loan);
            model.addAttribute("accounts", accountService.accountsForCustomer(loan.getCustomer().getId()));
            model.addAttribute("error", "Enter a valid amount greater than zero");
            return "admin/loans/detail";
        }
        try {
            loanService.makePayment(id, form);
            ra.addFlashAttribute("success", "Loan payment of $" + form.getAmount() + " accepted");
        } catch (BankingException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/loans/" + id;
    }
}