package com.epam.elms.service;

import com.epam.elms.dto.LoginRequest;
import com.epam.elms.dto.LoginResponse;
import com.epam.elms.dto.RegisterRequest;
import com.epam.elms.entity.Employee;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    Employee register(RegisterRequest request);
}
