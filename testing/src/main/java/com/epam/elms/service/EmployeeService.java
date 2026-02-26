package com.epam.elms.service;

import com.epam.elms.entity.Employee;
import java.util.Optional;

public interface EmployeeService {
    Optional<Employee> findByEmail(String email);
    Employee save(Employee employee);
}
