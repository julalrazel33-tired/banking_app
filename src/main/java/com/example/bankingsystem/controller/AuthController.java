package com.example.bankingsystem.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/")
    public String home() {
        return "redirect:/admin";
    }

    @GetMapping("/admin/login")
    public String loginPage(HttpServletRequest request, Model model) {
        if (request.getParameter("error") != null) {
            model.addAttribute("loginError", "Invalid username or password");
        }
        if (request.getParameter("logout") != null) {
            model.addAttribute("logoutMessage", "You have been logged out");
        }
        return "admin/login";
    }
}