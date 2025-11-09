package com.context.memorybook.service;

import com.context.memorybook.models.User;
import com.context.memorybook.models.UserPrincipal;
import com.context.memorybook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findByUsername(username);
        if(user == null){
            System.out.println("User not found 404");
            throw new UsernameNotFoundException("User Not Found ");
        }

        return new UserPrincipal(user);
    }
}
