package com.blooddonorconnect.project.repository;

import com.blooddonorconnect.project.model.DonationRequest;
import com.blooddonorconnect.project.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DonationRequestRepository extends JpaRepository<DonationRequest, Long> {
    List<DonationRequest> findByRequester(User requester);
    List<DonationRequest> findByRequesterAndStatus(User requester, DonationRequest.Status status);
}
