package com.blooddonorconnect.project.service;

import com.blooddonorconnect.project.dto.DonorProfileDTO;
import com.blooddonorconnect.project.dto.DonorSearchDTO;
import com.blooddonorconnect.project.model.DonorProfile;
import com.blooddonorconnect.project.repository.DonorProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchService {

    @Autowired
    private DonorProfileRepository donorProfileRepository;

    public List<DonorProfileDTO> searchDonors(DonorSearchDTO searchDTO) {
        List<DonorProfile> donors;

        // Search by blood group and location if location is provided
        if (searchDTO.getLocation() != null && !searchDTO.getLocation().trim().isEmpty()) {
            donors = donorProfileRepository.findByBloodGroupAndLocationAndAvailabilityStatus(
                    searchDTO.getBloodGroup(),
                    searchDTO.getLocation().trim(),
                    DonorProfile.AvailabilityStatus.AVAILABLE
            );
        } else {
            // Search by blood group only
            donors = donorProfileRepository.findByBloodGroupAndAvailabilityStatus(
                    searchDTO.getBloodGroup(),
                    DonorProfile.AvailabilityStatus.AVAILABLE
            );
        }

        // Filter eligible donors and convert to DTO
        return donors.stream()
                .filter(this::isDonorEligible)
                .map(this::convertToDonorProfileDTO)
                .collect(Collectors.toList());
    }

    public List<DonorProfileDTO> searchCompatibleDonors(DonorProfile.BloodGroup recipientBloodGroup, String location) {
        List<DonorProfile.BloodGroup> compatibleBloodGroups = getCompatibleBloodGroups(recipientBloodGroup);
        List<DonorProfile> allCompatibleDonors = new ArrayList<>();

        // Search for donors with compatible blood groups
        for (DonorProfile.BloodGroup bloodGroup : compatibleBloodGroups) {
            List<DonorProfile> donors;

            if (location != null && !location.trim().isEmpty()) {
                donors = donorProfileRepository.findByBloodGroupAndLocationAndAvailabilityStatus(
                        bloodGroup,
                        location.trim(),
                        DonorProfile.AvailabilityStatus.AVAILABLE
                );
            } else {
                donors = donorProfileRepository.findByBloodGroupAndAvailabilityStatus(
                        bloodGroup,
                        DonorProfile.AvailabilityStatus.AVAILABLE
                );
            }

            allCompatibleDonors.addAll(donors);
        }

        // Filter eligible donors and convert to DTO
        return allCompatibleDonors.stream()
                .distinct()
                .filter(this::isDonorEligible)
                .map(this::convertToDonorProfileDTO)
                .collect(Collectors.toList());
    }

    public List<DonorProfileDTO> searchDonorsByCity(DonorProfile.BloodGroup bloodGroup, String city) {
        List<DonorProfile> donors = donorProfileRepository.findByBloodGroupAndAvailabilityStatus(
                bloodGroup, DonorProfile.AvailabilityStatus.AVAILABLE);

        return donors.stream()
                .filter(donor -> donor.getCity().equalsIgnoreCase(city.trim()))
                .filter(this::isDonorEligible)
                .map(this::convertToDonorProfileDTO)
                .collect(Collectors.toList());
    }

    public List<DonorProfileDTO> searchDonorsByPincode(DonorProfile.BloodGroup bloodGroup, String pincode) {
        List<DonorProfile> donors = donorProfileRepository.findByBloodGroupAndAvailabilityStatus(
                bloodGroup, DonorProfile.AvailabilityStatus.AVAILABLE);

        return donors.stream()
                .filter(donor -> donor.getPincode().equals(pincode.trim()))
                .filter(this::isDonorEligible)
                .map(this::convertToDonorProfileDTO)
                .collect(Collectors.toList());
    }

    public long getAvailableDonorCount(DonorProfile.BloodGroup bloodGroup, String location) {
        List<DonorProfile> donors;

        if (location != null && !location.trim().isEmpty()) {
            donors = donorProfileRepository.findByBloodGroupAndLocationAndAvailabilityStatus(
                    bloodGroup,
                    location.trim(),
                    DonorProfile.AvailabilityStatus.AVAILABLE
            );
        } else {
            donors = donorProfileRepository.findByBloodGroupAndAvailabilityStatus(
                    bloodGroup,
                    DonorProfile.AvailabilityStatus.AVAILABLE
            );
        }

        return donors.stream()
                .filter(this::isDonorEligible)
                .count();
    }

    private boolean isDonorEligible(DonorProfile donor) {
        // Check if donor is available
        if (donor.getAvailabilityStatus() != DonorProfile.AvailabilityStatus.AVAILABLE) {
            return false;
        }

        // Check if unavailable until date has passed
        if (donor.getUnavailableUntil() != null && donor.getUnavailableUntil().isAfter(LocalDate.now())) {
            return false;
        }

        // Check if eligible for next donation
        if (donor.getNextEligibleDonationDate() != null && donor.getNextEligibleDonationDate().isAfter(LocalDate.now())) {
            return false;
        }

        return true;
    }

    private List<DonorProfile.BloodGroup> getCompatibleBloodGroups(DonorProfile.BloodGroup recipientBloodGroup) {
        List<DonorProfile.BloodGroup> compatibleGroups = new ArrayList<>();

        switch (recipientBloodGroup) {
            case A_POSITIVE:
                compatibleGroups.add(DonorProfile.BloodGroup.A_POSITIVE);
                compatibleGroups.add(DonorProfile.BloodGroup.A_NEGATIVE);
                compatibleGroups.add(DonorProfile.BloodGroup.O_POSITIVE);
                compatibleGroups.add(DonorProfile.BloodGroup.O_NEGATIVE);
                break;
            case A_NEGATIVE:
                compatibleGroups.add(DonorProfile.BloodGroup.A_NEGATIVE);
                compatibleGroups.add(DonorProfile.BloodGroup.O_NEGATIVE);
                break;
            case B_POSITIVE:
                compatibleGroups.add(DonorProfile.BloodGroup.B_POSITIVE);
                compatibleGroups.add(DonorProfile.BloodGroup.B_NEGATIVE);
                compatibleGroups.add(DonorProfile.BloodGroup.O_POSITIVE);
                compatibleGroups.add(DonorProfile.BloodGroup.O_NEGATIVE);
                break;
            case B_NEGATIVE:
                compatibleGroups.add(DonorProfile.BloodGroup.B_NEGATIVE);
                compatibleGroups.add(DonorProfile.BloodGroup.O_NEGATIVE);
                break;
            case AB_POSITIVE:
                // AB+ can receive from all blood groups (universal recipient)
                compatibleGroups.add(DonorProfile.BloodGroup.A_POSITIVE);
                compatibleGroups.add(DonorProfile.BloodGroup.A_NEGATIVE);
                compatibleGroups.add(DonorProfile.BloodGroup.B_POSITIVE);
                compatibleGroups.add(DonorProfile.BloodGroup.B_NEGATIVE);
                compatibleGroups.add(DonorProfile.BloodGroup.AB_POSITIVE);
                compatibleGroups.add(DonorProfile.BloodGroup.AB_NEGATIVE);
                compatibleGroups.add(DonorProfile.BloodGroup.O_POSITIVE);
                compatibleGroups.add(DonorProfile.BloodGroup.O_NEGATIVE);
                break;
            case AB_NEGATIVE:
                compatibleGroups.add(DonorProfile.BloodGroup.A_NEGATIVE);
                compatibleGroups.add(DonorProfile.BloodGroup.B_NEGATIVE);
                compatibleGroups.add(DonorProfile.BloodGroup.AB_NEGATIVE);
                compatibleGroups.add(DonorProfile.BloodGroup.O_NEGATIVE);
                break;
            case O_POSITIVE:
                compatibleGroups.add(DonorProfile.BloodGroup.O_POSITIVE);
                compatibleGroups.add(DonorProfile.BloodGroup.O_NEGATIVE);
                break;
            case O_NEGATIVE:
                compatibleGroups.add(DonorProfile.BloodGroup.O_NEGATIVE);
                break;
        }

        return compatibleGroups;
    }

    private DonorProfileDTO convertToDonorProfileDTO(DonorProfile donorProfile) {
        DonorProfileDTO dto = new DonorProfileDTO();
        dto.setDonorId(donorProfile.getDonorId());
        dto.setBloodGroup(donorProfile.getBloodGroup());
        dto.setCity(donorProfile.getCity());
        dto.setPincode(donorProfile.getPincode());
        dto.setAvailabilityStatus(donorProfile.getAvailabilityStatus());
        dto.setStatusReason(donorProfile.getStatusReason());
        dto.setUnavailableUntil(donorProfile.getUnavailableUntil());
        dto.setNextEligibleDonationDate(donorProfile.getNextEligibleDonationDate());

        // Set user details
        if (donorProfile.getUser() != null) {
            dto.setDonorName(donorProfile.getUser().getName());
            dto.setDonorContactNumber(donorProfile.getUser().getContactNumber());
        }

        return dto;
    }
}