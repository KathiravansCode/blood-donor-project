package com.blooddonorconnect.project.repository;

import com.blooddonorconnect.project.model.DonationHistory;
import com.blooddonorconnect.project.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DonationHistoryRepository extends JpaRepository<DonationHistory, Long> {
    List<DonationHistory> findByDonorOrderByDonationDateDesc(User donor);
    DonationHistory findTopByDonorOrderByDonationDateDesc(User donor);
}

