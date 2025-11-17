package com.context.memorybook.domain.user.service;

import com.context.memorybook.common.enums.Role;
import com.context.memorybook.domain.user.model.User;
import com.context.memorybook.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

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
