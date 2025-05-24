package com.blooddonorconnect.project.util;

import com.blooddonorconnect.project.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[6-9]\\d{9}$");

    private static final Pattern PINCODE_PATTERN =
            Pattern.compile("^[1-9][0-9]{5}$");

    private static final List<String> VALID_BLOOD_TYPES =
            Arrays.asList("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");

    public void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Email is required");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("Invalid email format");
        }
    }

    public void validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new ValidationException("Phone number is required");
        }
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new ValidationException("Invalid phone number format");
        }
    }

    public void validateBloodType(String bloodType) {
        if (bloodType == null || bloodType.trim().isEmpty()) {
            throw new ValidationException("Blood type is required");
        }
        if (!VALID_BLOOD_TYPES.contains(bloodType)) {
            throw new ValidationException("Invalid blood type");
        }
    }

    public void validatePincode(String pincode) {
        if (pincode == null || pincode.trim().isEmpty()) {
            throw new ValidationException("Pincode is required");
        }
        if (!PINCODE_PATTERN.matcher(pincode).matches()) {
            throw new ValidationException("Invalid pincode format");
        }
    }

    public void validateAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            throw new ValidationException("Date of birth is required");
        }

        int age = Period.between(dateOfBirth, LocalDate.now()).getYears();
        if (age < 18 || age > 65) {
            throw new ValidationException("Age must be between 18 and 65 years");
        }
    }

    public void validateDonationCooldown(LocalDate lastDonationDate) {
        if (lastDonationDate != null) {
            long daysSinceLastDonation = Period.between(lastDonationDate, LocalDate.now()).getDays();
            if (daysSinceLastDonation < 90) {
                throw new ValidationException("Must wait at least 90 days between donations");
            }
        }
    }

    public boolean isValidString(String str) {
        return str != null && !str.trim().isEmpty();
    }
}