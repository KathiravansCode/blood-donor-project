package com.blooddonorconnect.project.model;

import java.time.LocalDate;


import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "donor_profiles")
public class DonorProfile {

    @Id
    private Long donorId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "donor_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BloodGroup bloodGroup;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String pincode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AvailabilityStatus availabilityStatus;

    private String statusReason;

    private LocalDate unavailableUntil;

    private LocalDate nextEligibleDonationDate;

    public enum BloodGroup {
        A_POSITIVE("A+"),
        A_NEGATIVE("A-"),
        B_POSITIVE("B+"),
        B_NEGATIVE("B-"),
        AB_POSITIVE("AB+"),
        AB_NEGATIVE("AB-"),
        O_POSITIVE("O+"),
        O_NEGATIVE("O-");

        private final String display;

        BloodGroup(String display) {
            this.display = display;
        }

        public String getDisplay() {
            return display;
        }
    }

    public enum AvailabilityStatus {
        AVAILABLE,
        TEMPORARILY_UNAVAILABLE
    }
}