package com.epam.elms.controller;

import com.epam.elms.dto.LoginRequest;
import com.epam.elms.dto.LoginResponse;
import com.epam.elms.dto.RegisterRequest;
import com.epam.elms.entity.Employee;
import com.epam.elms.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/register")
    public ResponseEntity<Employee> register(@RequestBody RegisterRequest request) {
        Employee employee = authService.register(request);
        return ResponseEntity.ok(employee);
    }
}
