package com.example.bankingsystem.controller;

import com.example.bankingsystem.dto.CustomerForm;
import com.example.bankingsystem.entity.Customer;
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
@RequestMapping("/admin/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final AccountService accountService;
    private final LoanService loanService;

    public CustomerController(CustomerService customerService,
                              AccountService accountService,
                              LoanService loanService) {
        this.customerService = customerService;
        this.accountService = accountService;
        this.loanService = loanService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("customers", customerService.list());
        return "admin/customers/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        if (!model.containsAttribute("customerForm")) {
            model.addAttribute("customerForm", new CustomerForm());
        }
        return "admin/customers/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("customerForm") CustomerForm form,
                         BindingResult bindingResult, Model model, RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            return "admin/customers/form";
        }
        try {
            Customer customer = customerService.create(form);
            ra.addFlashAttribute("success", "Customer " + customer.getFullName() + " created successfully");
            return "redirect:/admin/customers";
        } catch (BankingException e) {
            model.addAttribute("error", e.getMessage());
            return "admin/customers/form";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Customer customer = customerService.get(id);
        model.addAttribute("customer", customer);
        model.addAttribute("accounts", accountService.accountsForCustomer(id));
        model.addAttribute("loans", loanService.loansForCustomer(id));
        return "admin/customers/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Customer customer = customerService.get(id);
        CustomerForm form = new CustomerForm();
        form.setFirstName(customer.getFirstName());
        form.setLastName(customer.getLastName());
        form.setEmail(customer.getEmail());
        form.setPhone(customer.getPhone());
        form.setAddress(customer.getAddress());
        model.addAttribute("customerForm", form);
        model.addAttribute("customerId", id);
        return "admin/customers/form";
    }

    @PostMapping("/{id}/update")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("customerForm") CustomerForm form,
                         BindingResult bindingResult, Model model, RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("customerId", id);
            return "admin/customers/form";
        }
        try {
            Customer customer = customerService.update(id, form);
            ra.addFlashAttribute("success", "Customer " + customer.getFullName() + " updated successfully");
            return "redirect:/admin/customers/" + id;
        } catch (BankingException e) {
            model.addAttribute("customerId", id);
            model.addAttribute("error", e.getMessage());
            return "admin/customers/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            customerService.delete(id);
            ra.addFlashAttribute("success", "Customer deleted successfully");
        } catch (BankingException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/customers";
    }
}