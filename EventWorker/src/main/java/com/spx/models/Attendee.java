package com.spx.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "attendees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Attendee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "qr_code", nullable = false, unique = true)
    private String qrCode;

    @Column(name = "cn")
    private String cn;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "partner_id", nullable = false)
    private String partnerId;

    @Column(name = "is_companion", nullable = false)
    private boolean isCompanion;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id_rif", nullable = false)
    private Campaign campaign;

}