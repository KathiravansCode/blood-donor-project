package com.blooddonorconnect.project.security;


import com.blooddonorconnect.project.model.User;
import com.blooddonorconnect.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // In our system, username is the contact number
        User user = userRepository.findByContactNumber(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with contact number: " + username));

        // Create Spring Security UserDetails based on our custom User entity
        return new org.springframework.security.core.userdetails.User(
                user.getContactNumber(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getUserType().name()))
        );
    }
}