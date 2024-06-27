package com.example.springbootfileupload.controller;

import com.example.springbootfileupload.entity.User;
import com.example.springbootfileupload.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/signup")
    public String showSignUpForm(Model model) {
        model.addAttribute("user", new User());
        return "signup";
    }

    @PostMapping("/signup")
    public String signUp(@ModelAttribute User user, Model model) {
        if (userService.findByUsername(user.getUsername()) != null) {
            model.addAttribute("error", "Username already exists");
            return "signup";
        }
        userService.saveUser(user);
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }
}
