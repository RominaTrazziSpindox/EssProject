package com.spx.dto;

import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendeeDTO {

    private String cn;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private String partnerId;
    private boolean isCompanion;
    private String qrCode;

}