package com.epam.elms.dto;

import lombok.*;
import java.time.LocalDate;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequestDto {
    private Long id;

    @NotNull
    private Long employeeId;

    private String employeeName;   // populated when returning data to manager

    @NotNull
    private Long leaveTypeId;

    private String leaveTypeName;  // populated when returning data to frontend

    @NotNull
    @FutureOrPresent(message = "Leave cannot be applied for past dates")
    private LocalDate startDate;

    @NotNull
    @FutureOrPresent(message = "Leave cannot be applied for past dates")
    private LocalDate endDate;

    @Size(max = 512)
    private String reason;

    private String status;         // populated when returning data to frontend (e.g. "PENDING", "APPROVED", "REJECTED")

    private String managerComment; // populated when returning data to frontend
}
