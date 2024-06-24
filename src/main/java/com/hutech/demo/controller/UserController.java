package com.hutech.demo.controller;

import com.hutech.demo.model.Role;
import com.hutech.demo.model.User;
import com.hutech.demo.service.RoleService;
import com.hutech.demo.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final RoleService roleService;

    @GetMapping("/users")
    public String getAllUsers(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "users/userList";
    }

    @GetMapping("/users/search")
    public String searchUsers(@RequestParam("username") String username, Model model) {
        Optional<User> user = userService.findByUsername(username);
        if (user.isPresent()) {
            model.addAttribute("users", List.of(user.get()));
        } else {
            model.addAttribute("users", List.of());
        }
        return "users/searchUser";
    }

    @GetMapping("/login")
    public String login() {
        return "users/login";
    }

    @GetMapping("/register")
    public String register(@NotNull Model model) {
        model.addAttribute("user", new User());
        return "users/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("user") User user,
                           @NotNull BindingResult bindingResult,
                           Model model) {
        if (bindingResult.hasErrors()) {
            var errors = bindingResult.getAllErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toArray(String[]::new);
            model.addAttribute("errors", errors);
            return "users/register";
        }
        userService.save(user);
        userService.setDefaultRole(user.getUsername());
        return "redirect:/login";
    }

    @GetMapping("/users/{username}/edit")
    public String editUserRoles(@PathVariable String username, Model model) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        model.addAttribute("user", user);
        model.addAttribute("roles", roleService.getAllRoles());
        return "users/editUserRoles";
    }

    @PostMapping("/users/{username}/edit")
    public String updateUserRoles(@PathVariable String username, @RequestParam Set<Long> roleIds) {
        userService.updateUserRoles(username, roleIds);
        return "redirect:/users";
    }
}
