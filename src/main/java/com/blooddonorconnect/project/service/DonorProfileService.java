package com.blooddonorconnect.project.service;

import com.blooddonorconnect.project.dto.DonorProfileDTO;
import com.blooddonorconnect.project.model.DonorProfile;
import com.blooddonorconnect.project.model.User;
import com.blooddonorconnect.project.repository.DonorProfileRepository;
import com.blooddonorconnect.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DonorProfileService {

    @Autowired
    private DonorProfileRepository donorProfileRepository;

    @Autowired
    private UserRepository userRepository;

    public DonorProfileDTO createDonorProfile(Long userId, DonorProfileDTO donorProfileDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getUserType().equals(User.UserType.DONOR)) {
            throw new RuntimeException("User is not a donor");
        }

        // Check if profile already exists
        if (donorProfileRepository.existsById(userId)) {
            throw new RuntimeException("Donor profile already exists");
        }

        DonorProfile donorProfile = new DonorProfile();
        donorProfile.setDonorId(userId);
        donorProfile.setUser(user);
        donorProfile.setBloodGroup(donorProfileDTO.getBloodGroup());
        donorProfile.setCity(donorProfileDTO.getCity());
        donorProfile.setPincode(donorProfileDTO.getPincode());
        donorProfile.setAvailabilityStatus(donorProfileDTO.getAvailabilityStatus());
        donorProfile.setStatusReason(donorProfileDTO.getStatusReason());
        donorProfile.setUnavailableUntil(donorProfileDTO.getUnavailableUntil());
        donorProfile.setNextEligibleDonationDate(donorProfileDTO.getNextEligibleDonationDate());

        DonorProfile savedProfile = donorProfileRepository.save(donorProfile);
        return convertToDonorProfileDTO(savedProfile);
    }

    public DonorProfileDTO getDonorProfile(Long userId) {
        DonorProfile donorProfile = donorProfileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Donor profile not found"));

        return convertToDonorProfileDTO(donorProfile);
    }

    public DonorProfileDTO updateDonorProfile(Long userId, DonorProfileDTO donorProfileDTO) {
        DonorProfile donorProfile = donorProfileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Donor profile not found"));

        donorProfile.setBloodGroup(donorProfileDTO.getBloodGroup());
        donorProfile.setCity(donorProfileDTO.getCity());
        donorProfile.setPincode(donorProfileDTO.getPincode());
        donorProfile.setAvailabilityStatus(donorProfileDTO.getAvailabilityStatus());
        donorProfile.setStatusReason(donorProfileDTO.getStatusReason());
        donorProfile.setUnavailableUntil(donorProfileDTO.getUnavailableUntil());
        donorProfile.setNextEligibleDonationDate(donorProfileDTO.getNextEligibleDonationDate());

        DonorProfile updatedProfile = donorProfileRepository.save(donorProfile);
        return convertToDonorProfileDTO(updatedProfile);
    }

    public DonorProfileDTO updateAvailabilityStatus(Long userId, DonorProfile.AvailabilityStatus status,
                                                    String reason, LocalDate unavailableUntil) {
        DonorProfile donorProfile = donorProfileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Donor profile not found"));

        donorProfile.setAvailabilityStatus(status);
        donorProfile.setStatusReason(reason);
        donorProfile.setUnavailableUntil(unavailableUntil);

        DonorProfile updatedProfile = donorProfileRepository.save(donorProfile);
        return convertToDonorProfileDTO(updatedProfile);
    }

    public List<DonorProfileDTO> getAvailableDonors(DonorProfile.BloodGroup bloodGroup) {
        List<DonorProfile> donors = donorProfileRepository.findByBloodGroupAndAvailabilityStatus(
                bloodGroup, DonorProfile.AvailabilityStatus.AVAILABLE);

        return donors.stream()
                .map(this::convertToDonorProfileDTO)
                .collect(Collectors.toList());
    }

    public List<DonorProfileDTO> searchDonors(DonorProfile.BloodGroup bloodGroup, String location) {
        List<DonorProfile> donors;

        if (location != null && !location.trim().isEmpty()) {
            donors = donorProfileRepository.findByBloodGroupAndLocationAndAvailabilityStatus(
                    bloodGroup, location.trim(), DonorProfile.AvailabilityStatus.AVAILABLE);
        } else {
            donors = donorProfileRepository.findByBloodGroupAndAvailabilityStatus(
                    bloodGroup, DonorProfile.AvailabilityStatus.AVAILABLE);
        }

        return donors.stream()
                .filter(this::isDonorEligible)
                .map(this::convertToDonorProfileDTO)
                .collect(Collectors.toList());
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