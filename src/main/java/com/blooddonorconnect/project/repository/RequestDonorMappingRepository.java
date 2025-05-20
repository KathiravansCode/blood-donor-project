package com.blooddonorconnect.project.repository;

import com.blooddonorconnect.project.model.DonationRequest;
import com.blooddonorconnect.project.model.RequestDonorMapping;
import com.blooddonorconnect.project.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequestDonorMappingRepository extends JpaRepository<RequestDonorMapping, Long> {
    List<RequestDonorMapping> findByDonor(User donor);
    List<RequestDonorMapping> findByRequest(DonationRequest request);
    List<RequestDonorMapping> findByDonorAndStatus(User donor, RequestDonorMapping.Status status);
    Optional<RequestDonorMapping> findByRequestAndDonor(DonationRequest request, User donor);
}
