package com.epam.elms.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "leave_balance")
public class LeaveBalance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable=false)
    private Employee employee;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "leave_type_id", nullable=false)
    private LeaveType leaveType;

    @Column(nullable=false)
    private Double balance; // in days
}
