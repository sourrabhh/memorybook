package com.context.memorybook.models;

import com.context.memorybook.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;

    private String lastName;

    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate(){
        createdAt = LocalDateTime.now();
        if(this.username == null || this.username.trim().isEmpty()){
            this.username = this.email;
        }
    }

    // Check the Authorities of user
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }
}
