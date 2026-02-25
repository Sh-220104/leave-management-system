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
    private Long employeeId; // Added employeeId field for creation
    @NotNull
    private Long leaveTypeId;
    @NotNull
    @FutureOrPresent(message = "Leave cannot be applied for past dates")
    private LocalDate startDate;
    @NotNull
    @FutureOrPresent(message = "Leave cannot be applied for past dates")
    private LocalDate endDate;
    @Size(max = 512)
    private String reason; // was 'notes', unified as reason
}
