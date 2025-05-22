package com.blooddonorconnect.project.service;

import com.blooddonorconnect.project.dto.DonationRequestDTO;
import com.blooddonorconnect.project.dto.RequestResponseDTO;
import com.blooddonorconnect.project.model.*;
import com.blooddonorconnect.project.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DonationRequestService {

    @Autowired
    private DonationRequestRepository donationRequestRepository;

    @Autowired
    private RequestDonorMappingRepository requestDonorMappingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DonorProfileRepository donorProfileRepository;

    @Autowired
    private NotificationService notificationService;

    public DonationRequestDTO createDonationRequest(Long requesterId, DonationRequestDTO requestDTO) {
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        if (!requester.getUserType().equals(User.UserType.REQUESTER)) {
            throw new RuntimeException("User is not a requester");
        }

        DonationRequest request = new DonationRequest();
        request.setRequester(requester);
        request.setBloodGroupNeeded(requestDTO.getBloodGroupNeeded());
        request.setLocation(requestDTO.getLocation());
        request.setHospitalName(requestDTO.getHospitalName());
        request.setUrgency(requestDTO.getUrgency());
        request.setMessage(requestDTO.getMessage());
        request.setStatus(DonationRequest.Status.ACTIVE);

        DonationRequest savedRequest = donationRequestRepository.save(request);

        // Notify eligible donors
        notifyEligibleDonors(savedRequest);

        return convertToDonationRequestDTO(savedRequest);
    }

    public DonationRequestDTO getDonationRequest(Long requestId) {
        DonationRequest request = donationRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Donation request not found"));

        return convertToDonationRequestDTO(request);
    }

    public List<DonationRequestDTO> getRequesterRequests(Long requesterId) {
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        List<DonationRequest> requests = donationRequestRepository.findByRequester(requester);

        return requests.stream()
                .map(this::convertToDonationRequestDTO)
                .collect(Collectors.toList());
    }

    public List<DonationRequestDTO> getDonorRequests(Long donorId) {
        User donor = userRepository.findById(donorId)
                .orElseThrow(() -> new RuntimeException("Donor not found"));

        List<RequestDonorMapping> mappings = requestDonorMappingRepository.findByDonor(donor);

        return mappings.stream()
                .map(mapping -> convertToDonationRequestDTO(mapping.getRequest()))
                .collect(Collectors.toList());
    }

    public void respondToRequest(Long donorId, RequestResponseDTO responseDTO) {
        User donor = userRepository.findById(donorId)
                .orElseThrow(() -> new RuntimeException("Donor not found"));

        DonationRequest request = donationRequestRepository.findById(responseDTO.getRequestId())
                .orElseThrow(() -> new RuntimeException("Donation request not found"));

        // Check if mapping already exists
        RequestDonorMapping existingMapping = requestDonorMappingRepository
                .findByRequestAndDonor(request, donor)
                .orElse(null);

        if (existingMapping != null) {
            existingMapping.setStatus(responseDTO.getStatus());
            requestDonorMappingRepository.save(existingMapping);
        } else {
            RequestDonorMapping mapping = new RequestDonorMapping();
            mapping.setRequest(request);
            mapping.setDonor(donor);
            mapping.setStatus(responseDTO.getStatus());
            requestDonorMappingRepository.save(mapping);
        }

        // Create notification for requester
        String message = String.format("Donor %s has %s your blood donation request",
                donor.getName(),
                responseDTO.getStatus().toString().toLowerCase());

        notificationService.createNotification(
                request.getRequester().getId(),
                responseDTO.getStatus() == RequestDonorMapping.Status.ACCEPTED ?
                        Notification.NotificationType.REQUEST_ACCEPTED :
                        Notification.NotificationType.REQUEST_DECLINED,
                request.getId(),
                message
        );
    }

    public DonationRequestDTO updateRequestStatus(Long requestId, DonationRequest.Status status) {
        DonationRequest request = donationRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Donation request not found"));

        request.setStatus(status);
        DonationRequest updatedRequest = donationRequestRepository.save(request);

        return convertToDonationRequestDTO(updatedRequest);
    }

    public void fulfillRequest(Long requestId, Long donorId) {
        DonationRequest request = donationRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Donation request not found"));

        User donor = userRepository.findById(donorId)
                .orElseThrow(() -> new RuntimeException("Donor not found"));

        // Update request status
        request.setStatus(DonationRequest.Status.FULFILLED);
        donationRequestRepository.save(request);

        // Update donor availability and next eligible date (3 months cooldown)
        DonorProfile donorProfile = donorProfileRepository.findById(donorId)
                .orElseThrow(() -> new RuntimeException("Donor profile not found"));

        donorProfile.setNextEligibleDonationDate(LocalDate.now().plusMonths(3));
        donorProfileRepository.save(donorProfile);

        // Create notification for donor
        notificationService.createNotification(
                donorId,
                Notification.NotificationType.DONATION_LOGGED,
                requestId,
                "Thank you for your donation! You'll be eligible to donate again after " +
                        donorProfile.getNextEligibleDonationDate()
        );
    }

    private void notifyEligibleDonors(DonationRequest request) {
        // Find eligible donors
        List<DonorProfile> eligibleDonors = donorProfileRepository
                .findByBloodGroupAndLocationAndAvailabilityStatus(
                        request.getBloodGroupNeeded(),
                        request.getLocation(),
                        DonorProfile.AvailabilityStatus.AVAILABLE
                );

        // Create request-donor mappings and notifications
        for (DonorProfile donor : eligibleDonors) {
            // Create mapping
            RequestDonorMapping mapping = new RequestDonorMapping();
            mapping.setRequest(request);
            mapping.setDonor(donor.getUser());
            mapping.setStatus(RequestDonorMapping.Status.PENDING);
            requestDonorMappingRepository.save(mapping);

            // Create notification
            String message = String.format("New %s blood donation request from %s at %s",
                    request.getBloodGroupNeeded().getDisplay(),
                    request.getRequester().getName(),
                    request.getHospitalName());

            notificationService.createNotification(
                    donor.getDonorId(),
                    Notification.NotificationType.REQUEST_RECEIVED,
                    request.getId(),
                    message
            );
        }
    }

    private DonationRequestDTO convertToDonationRequestDTO(DonationRequest request) {
        DonationRequestDTO dto = new DonationRequestDTO();
        dto.setId(request.getId());
        dto.setRequesterId(request.getRequester().getId());
        dto.setBloodGroupNeeded(request.getBloodGroupNeeded());
        dto.setLocation(request.getLocation());
        dto.setHospitalName(request.getHospitalName());
        dto.setUrgency(request.getUrgency());
        dto.setMessage(request.getMessage());
        dto.setStatus(request.getStatus());
        dto.setCreatedAt(request.getCreatedAt());
        dto.setUpdatedAt(request.getUpdatedAt());

        // Set requester details
        dto.setRequesterName(request.getRequester().getName());
        dto.setRequesterContactNumber(request.getRequester().getContactNumber());

        return dto;
    }
}