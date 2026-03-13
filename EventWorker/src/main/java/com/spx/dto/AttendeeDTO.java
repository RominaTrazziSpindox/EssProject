package com.spx.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendeeDTO {

    @NotBlank(message="First name cannot be blank")
    private String firstName;

    @NotBlank(message="Last name cannot be blank")
    private String lastName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @NotNull(message="PartnerId cannot be null")
    private String partnerId;

    private String cn; // Companion name

    @NotNull(message="IsCompanion cannot be null")
    private Boolean isCompanion;

    @NotBlank(message="The QR code cannot be blank")
    private String qrCode;

}