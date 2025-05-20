package com.blooddonorconnect.project.repository;

import com.blooddonorconnect.project.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByContactNumber(String contactNumber);
    Optional<User> findByEmail(String email);
    boolean existsByContactNumber(String contactNumber);
    boolean existsByEmail(String email);
}
