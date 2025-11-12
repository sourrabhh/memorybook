package com.context.memorybook.service;

import com.context.memorybook.enums.Role;
import com.context.memorybook.models.User;
import com.context.memorybook.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public String registerUser(User user){
        if(userRepository.existsByEmail(user.getEmail())){
            return "Email Already Existed";
        }
        if (user.getRole() == null) {
            user.setRole(Role.USER); // ensure default role
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return "User registered successfully";
    }
}
