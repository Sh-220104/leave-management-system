package com.epam.elms.unit.service;

import com.epam.elms.entity.Employee;
import com.epam.elms.repository.EmployeeRepository;
import com.epam.elms.service.impl.EmployeeServiceImpl;
import com.epam.elms.utils.TestDataFactory;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link EmployeeServiceImpl}.
 * Uses explicit constructor injection for reliable mock wiring.
 */
public class EmployeeServiceImplTest {

    @Mock private EmployeeRepository employeeRepository;

    private EmployeeServiceImpl employeeService;
    private AutoCloseable       mocks;

    @BeforeMethod
    public void setUp() {
        mocks           = MockitoAnnotations.openMocks(this);
        employeeService = new EmployeeServiceImpl(employeeRepository);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        mocks.close();
    }

    // ── findByEmail ───────────────────────────────────────────────────────────

    @Test(description = "findByEmail – existing email returns Optional containing the employee")
    public void findByEmail_existingEmail_returnsEmployee() {
        Employee employee = TestDataFactory.buildEmployee(1L, "Alice", "alice@epam.com", "hash");
        when(employeeRepository.findByEmail("alice@epam.com")).thenReturn(Optional.of(employee));

        Optional<Employee> result = employeeService.findByEmail("alice@epam.com");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getEmail()).isEqualTo("alice@epam.com");
        verify(employeeRepository).findByEmail("alice@epam.com");
    }

    @Test(description = "findByEmail – unknown email returns empty Optional")
    public void findByEmail_unknownEmail_returnsEmpty() {
        when(employeeRepository.findByEmail("ghost@epam.com")).thenReturn(Optional.empty());

        Optional<Employee> result = employeeService.findByEmail("ghost@epam.com");

        assertThat(result).isEmpty();
        verify(employeeRepository).findByEmail("ghost@epam.com");
    }

    @Test(description = "findByEmail – delegates to repository and returns exactly what the repository returns")
    public void findByEmail_delegatesToRepository() {
        Employee employee = TestDataFactory.buildEmployee(2L, "Bob", "bob@epam.com", "hash");
        when(employeeRepository.findByEmail("bob@epam.com")).thenReturn(Optional.of(employee));

        Optional<Employee> result = employeeService.findByEmail("bob@epam.com");

        assertThat(result).contains(employee);
    }

    @Test(description = "findByEmail – null email returns empty Optional without throwing")
    public void findByEmail_nullEmail_returnsEmpty() {
        when(employeeRepository.findByEmail(null)).thenReturn(Optional.empty());

        Optional<Employee> result = employeeService.findByEmail(null);

        assertThat(result).isEmpty();
    }

    // ── DataProvider for multiple emails ──────────────────────────────────────

    @DataProvider(name = "emails")
    public Object[][] emailsProvider() {
        return new Object[][] {
            { "alice@epam.com", true  },
            { "bob@epam.com",   true  },
            { "ghost@epam.com", false },
        };
    }

    @Test(dataProvider = "emails",
          description = "findByEmail – correctly signals presence/absence for various emails")
    public void findByEmail_variousEmails_correctPresence(String email, boolean shouldBePresent) {
        Employee emp = TestDataFactory.buildEmployee(99L, "Test", email, "hash");
        when(employeeRepository.findByEmail(email))
                .thenReturn(shouldBePresent ? Optional.of(emp) : Optional.empty());

        Optional<Employee> result = employeeService.findByEmail(email);

        assertThat(result.isPresent()).isEqualTo(shouldBePresent);
    }

    // ── save ──────────────────────────────────────────────────────────────────

    @Test(description = "save – delegates to repository and returns the persisted entity")
    public void save_validEmployee_returnsSavedEmployee() {
        Employee toSave = TestDataFactory.buildEmployee(null, "Carol", "carol@epam.com", "hash");
        Employee saved  = TestDataFactory.buildEmployee(5L,   "Carol", "carol@epam.com", "hash");

        when(employeeRepository.save(toSave)).thenReturn(saved);

        Employee result = employeeService.save(toSave);

        assertThat(result.getId()).isEqualTo(5L);
        assertThat(result.getEmail()).isEqualTo("carol@epam.com");
        verify(employeeRepository).save(toSave);
    }

    @Test(description = "save – the returned employee has the ID assigned by the repository")
    public void save_newEmployee_idIsAssigned() {
        Employee unsaved   = TestDataFactory.buildEmployee(null, "Dave", "dave@epam.com", "hash");
        Employee persisted = TestDataFactory.buildEmployee(10L,  "Dave", "dave@epam.com", "hash");

        when(employeeRepository.save(any(Employee.class))).thenReturn(persisted);

        Employee result = employeeService.save(unsaved);

        assertThat(result.getId()).isNotNull().isEqualTo(10L);
    }

    @Test(description = "save – repository is called exactly once per save invocation")
    public void save_repositoryCalledExactlyOnce() {
        Employee emp = TestDataFactory.buildEmployee(1L, "Eve", "eve@epam.com", "hash");
        when(employeeRepository.save(emp)).thenReturn(emp);

        employeeService.save(emp);

        verify(employeeRepository, times(1)).save(emp);
    }

    @Test(description = "save – email and name are preserved after save")
    public void save_employeeFields_preserved() {
        Employee emp       = TestDataFactory.buildEmployee(null, "Frank", "frank@epam.com", "hash");
        Employee persisted = TestDataFactory.buildEmployee(7L,   "Frank", "frank@epam.com", "hash");

        when(employeeRepository.save(emp)).thenReturn(persisted);

        Employee result = employeeService.save(emp);

        assertThat(result.getName()).isEqualTo("Frank");
        assertThat(result.getEmail()).isEqualTo("frank@epam.com");
    }
}
