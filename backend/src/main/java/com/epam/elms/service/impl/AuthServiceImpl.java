package com.epam.elms.service.impl;

import com.epam.elms.dto.LoginRequest;
import com.epam.elms.dto.LoginResponse;
import com.epam.elms.dto.RegisterRequest;
import com.epam.elms.entity.Employee;
import com.epam.elms.entity.LeaveBalance;
import com.epam.elms.entity.LeaveType;
import com.epam.elms.repository.EmployeeRepository;
import com.epam.elms.repository.LeaveTypeRepository;
import com.epam.elms.repository.LeaveBalanceRepository;
import com.epam.elms.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;

    @Override
    public LoginResponse login(LoginRequest request) {
        Optional<Employee> employeeOpt = employeeRepository.findByEmail(request.getEmail());
        if (employeeOpt.isEmpty()) {
            System.out.println("Login attempt failed: user not found for email " + request.getEmail());
            throw new RuntimeException("Invalid email or password");
        }
        Employee employee = employeeOpt.get();
        boolean passwordMatch = passwordEncoder.matches(request.getPassword(), employee.getPassword());
        System.out.println("Login request email: " + request.getEmail());
        System.out.println("DB hash: " + employee.getPassword());
        System.out.println("DB roles: " + employee.getRoles());
        System.out.println("Password match? " + passwordMatch);
        if (!passwordMatch) {
            System.out.println("Login failed: password mismatch for " + request.getEmail());
            throw new RuntimeException("Invalid email or password");
        }
        String jwt = jwtUtil.generateToken(employee);
        return LoginResponse.builder()
                .employeeId(employee.getId())
                .jwt(jwt)
                .name(employee.getName())
                .email(employee.getEmail())
                .role(employee.getRoles() != null && !employee.getRoles().isEmpty() ? employee.getRoles().iterator().next() : "EMPLOYEE")
                .build();
    }

    @Override
    public Employee register(RegisterRequest request) {
        Employee employee = new Employee();
        employee.setEmail(request.getEmail());
        employee.setName(request.getName());
        employee.setPassword(passwordEncoder.encode(request.getPassword()));

        Set<String> roles = new HashSet<>();
        roles.add(request.getRole() != null ? request.getRole() : "EMPLOYEE");
        employee.setRoles(roles);

        // Save employee first to get the ID
        employee = employeeRepository.save(employee);

        // Auto-create leave balances for each leave type
        List<LeaveType> leaveTypes = leaveTypeRepository.findAll();
        for (LeaveType leaveType : leaveTypes) {
            LeaveBalance balance = new LeaveBalance();
            balance.setEmployee(employee);
            balance.setLeaveType(leaveType);
            balance.setBalance(20.0); // Default starting balance, adjust as required
            leaveBalanceRepository.save(balance);
        }

        return employee;
    }
}
