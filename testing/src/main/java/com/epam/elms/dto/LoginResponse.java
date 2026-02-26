package com.epam.elms.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private Long employeeId;
    private String jwt;
    private String name;
    private String email;
    private String role;
}
