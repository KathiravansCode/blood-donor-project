package com.blooddonorconnect.project.repository;

import com.blooddonorconnect.project.model.DonorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DonorProfileRepository extends JpaRepository<DonorProfile, Long> {
    List<DonorProfile> findByBloodGroupAndAvailabilityStatus(DonorProfile.BloodGroup bloodGroup, DonorProfile.AvailabilityStatus availabilityStatus);

    @Query("SELECT d FROM DonorProfile d WHERE d.bloodGroup = :bloodGroup AND d.availabilityStatus = :availabilityStatus AND (d.city = :location OR d.pincode = :location)")
    List<DonorProfile> findByBloodGroupAndLocationAndAvailabilityStatus(DonorProfile.BloodGroup bloodGroup, String location, DonorProfile.AvailabilityStatus availabilityStatus);
}