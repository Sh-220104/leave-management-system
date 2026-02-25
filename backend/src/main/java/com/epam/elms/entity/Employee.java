package com.epam.elms.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "employee")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true)
    private String email;

    @Column(nullable=false)
    private String name;

    @Column(nullable=false)
    private String password; // encrypted

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name="employee_roles", joinColumns=@JoinColumn(name="employee_id"))
    @Column(name="role")
    private Set<String> roles;
}
