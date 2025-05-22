package com.blooddonorconnect.project.service;

import com.blooddonorconnect.project.dto.DonationHistoryDTO;
import com.blooddonorconnect.project.model.DonationHistory;
import com.blooddonorconnect.project.model.DonationRequest;
import com.blooddonorconnect.project.model.User;
import com.blooddonorconnect.project.repository.DonationHistoryRepository;
import com.blooddonorconnect.project.repository.DonationRequestRepository;
import com.blooddonorconnect.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DonationHistoryService {

    @Autowired
    private DonationHistoryRepository donationHistoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DonationRequestRepository donationRequestRepository;

    public DonationHistoryDTO logDonation(DonationHistoryDTO donationHistoryDTO) {
        User donor = userRepository.findById(donationHistoryDTO.getDonorId())
                .orElseThrow(() -> new RuntimeException("Donor not found"));

        if (!donor.getUserType().equals(User.UserType.DONOR)) {
            throw new RuntimeException("User is not a donor");
        }

        DonationHistory donationHistory = new DonationHistory();
        donationHistory.setDonor(donor);
        donationHistory.setDonationDate(donationHistoryDTO.getDonationDate());
        donationHistory.setLocation(donationHistoryDTO.getLocation());
        donationHistory.setNotes(donationHistoryDTO.getNotes());

        // Set requester if provided
        if (donationHistoryDTO.getRequesterId() != null) {
            User requester = userRepository.findById(donationHistoryDTO.getRequesterId())
                    .orElseThrow(() -> new RuntimeException("Requester not found"));
            donationHistory.setRequester(requester);
        }

        // Set request if provided
        if (donationHistoryDTO.getRequestId() != null) {
            DonationRequest request = donationRequestRepository.findById(donationHistoryDTO.getRequestId())
                    .orElseThrow(() -> new RuntimeException("Donation request not found"));
            donationHistory.setRequest(request);
        }

        DonationHistory savedHistory = donationHistoryRepository.save(donationHistory);
        return convertToDonationHistoryDTO(savedHistory);
    }

    public List<DonationHistoryDTO> getDonorHistory(Long donorId) {
        User donor = userRepository.findById(donorId)
                .orElseThrow(() -> new RuntimeException("Donor not found"));

        List<DonationHistory> historyList = donationHistoryRepository.findByDonorOrderByDonationDateDesc(donor);

        return historyList.stream()
                .map(this::convertToDonationHistoryDTO)
                .collect(Collectors.toList());
    }

    public DonationHistoryDTO getLastDonation(Long donorId) {
        User donor = userRepository.findById(donorId)
                .orElseThrow(() -> new RuntimeException("Donor not found"));

        DonationHistory lastDonation = donationHistoryRepository.findTopByDonorOrderByDonationDateDesc(donor);

        if (lastDonation == null) {
            return null;
        }

        return convertToDonationHistoryDTO(lastDonation);
    }

    public boolean canDonateToday(Long donorId) {
        DonationHistoryDTO lastDonation = getLastDonation(donorId);

        if (lastDonation == null) {
            return true; // No previous donations
        }

        // Check if 3 months (90 days) have passed since last donation
        LocalDate lastDonationDate = lastDonation.getDonationDate();
        LocalDate eligibleDate = lastDonationDate.plusDays(90);

        return LocalDate.now().isAfter(eligibleDate) || LocalDate.now().isEqual(eligibleDate);
    }

    public LocalDate getNextEligibleDate(Long donorId) {
        DonationHistoryDTO lastDonation = getLastDonation(donorId);

        if (lastDonation == null) {
            return LocalDate.now(); // Can donate immediately if no previous donations
        }

        // Next eligible date is 3 months (90 days) after last donation
        return lastDonation.getDonationDate().plusDays(90);
    }

    public int getTotalDonationsCount(Long donorId) {
        User donor = userRepository.findById(donorId)
                .orElseThrow(() -> new RuntimeException("Donor not found"));

        List<DonationHistory> historyList = donationHistoryRepository.findByDonorOrderByDonationDateDesc(donor);
        return historyList.size();
    }

    public DonationHistoryDTO updateDonationHistory(Long historyId, DonationHistoryDTO donationHistoryDTO) {
        DonationHistory donationHistory = donationHistoryRepository.findById(historyId)
                .orElseThrow(() -> new RuntimeException("Donation history not found"));

        donationHistory.setDonationDate(donationHistoryDTO.getDonationDate());
        donationHistory.setLocation(donationHistoryDTO.getLocation());
        donationHistory.setNotes(donationHistoryDTO.getNotes());

        DonationHistory updatedHistory = donationHistoryRepository.save(donationHistory);
        return convertToDonationHistoryDTO(updatedHistory);
    }

    public void deleteDonationHistory(Long historyId) {
        DonationHistory donationHistory = donationHistoryRepository.findById(historyId)
                .orElseThrow(() -> new RuntimeException("Donation history not found"));

        donationHistoryRepository.delete(donationHistory);
    }

    private DonationHistoryDTO convertToDonationHistoryDTO(DonationHistory donationHistory) {
        DonationHistoryDTO dto = new DonationHistoryDTO();
        dto.setId(donationHistory.getId());
        dto.setDonorId(donationHistory.getDonor().getId());
        dto.setDonationDate(donationHistory.getDonationDate());
        dto.setLocation(donationHistory.getLocation());
        dto.setNotes(donationHistory.getNotes());

        // Set donor details
        dto.setDonorName(donationHistory.getDonor().getName());

        // Set requester details if available
        if (donationHistory.getRequester() != null) {
            dto.setRequesterId(donationHistory.getRequester().getId());
            dto.setRequesterName(donationHistory.getRequester().getName());
        }

        // Set request details if available
        if (donationHistory.getRequest() != null) {
            dto.setRequestId(donationHistory.getRequest().getId());
        }

        return dto;
    }
}