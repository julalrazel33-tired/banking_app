package com.example.bankingsystem.controller;

import com.example.bankingsystem.dto.AdminForm;
import com.example.bankingsystem.entity.Admin;
import com.example.bankingsystem.exception.BankingException;
import com.example.bankingsystem.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
@RequestMapping("/admin/admins")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("admins", adminService.list());
        return "admin/admins/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        if (!model.containsAttribute("adminForm")) {
            model.addAttribute("adminForm", new AdminForm());
        }
        return "admin/admins/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("adminForm") AdminForm form,
                         BindingResult bindingResult, Model model, RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            return "admin/admins/form";
        }
        try {
            adminService.create(form);
            ra.addFlashAttribute("success", "Admin '" + form.getUsername() + "' created successfully");
            return "redirect:/admin/admins";
        } catch (BankingException e) {
            model.addAttribute("error", e.getMessage());
            return "admin/admins/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Admin admin = adminService.get(id);
        AdminForm form = new AdminForm();
        form.setUsername(admin.getUsername());
        form.setRole(admin.getRole());
        form.setActive(admin.isActive());
        model.addAttribute("adminForm", form);
        model.addAttribute("adminId", id);
        return "admin/admins/form";
    }

    @PostMapping("/{id}/update")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("adminForm") AdminForm form,
                         BindingResult bindingResult, Model model, RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("adminId", id);
            return "admin/admins/form";
        }
        try {
            adminService.update(id, form);
            ra.addFlashAttribute("success", "Admin '" + form.getUsername() + "' updated successfully");
            return "redirect:/admin/admins";
        } catch (BankingException e) {
            model.addAttribute("adminId", id);
            model.addAttribute("error", e.getMessage());
            return "admin/admins/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            Long currentAdminId = currentAdminId();
            adminService.delete(id, currentAdminId);
            ra.addFlashAttribute("success", "Admin deleted successfully");
        } catch (BankingException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/admins";
    }

    private Long currentAdminId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return adminService.getByUsername(auth.getName()).getId();
    }
}