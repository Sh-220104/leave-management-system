package com.epam.elms.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "leave_request")
public class LeaveRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="employee_id", nullable=false)
    private Employee employee;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="leave_type_id", nullable=false)
    private LeaveType leaveType;

    @Column(nullable=false)
    private LocalDate startDate;

    @Column(nullable=false)
    private LocalDate endDate;

    @Column(length = 512)
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private LeaveStatus status;

    private String managerComment;

    private LocalDate createdOn;
    private LocalDate decisionOn;
}
