package com.spx.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.LocalDate;


@Entity
public class Attendee {

    @Id
    private String qrCode;

    private String cn;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;

    private String partnerId;
    private Boolean isCompanion;

    @ManyToOne
    @JoinColumn(name = "campaign_id")
    private Campaign campaign;
}