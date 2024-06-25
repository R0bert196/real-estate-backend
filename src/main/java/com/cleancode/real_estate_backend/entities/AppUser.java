package com.cleancode.real_estate_backend.entities;



import com.cleancode.real_estate_backend.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppUser implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;


    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column
    private String password;

    @Column
    private Boolean enabled = false;

    @Column(length = 64)
    private String verificationCode;

    @Column(length = 64)
    private String resetPasswordCode;

    @Temporal(TemporalType.TIMESTAMP)
    private Long createTs;

    @Column
    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<Role> role = new HashSet<>();


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();
        for (Role r : this.role) {
            SimpleGrantedAuthority sga = new SimpleGrantedAuthority(r.name());
            authorities.add(sga);
        }
        return authorities;
    }

    @PrePersist
    private void prePersist() {
        this.verificationCode = RandomStringUtils.random(6, false, true);
        this.createTs = Instant.now().toEpochMilli();
        this.enabled = false;
    }


    @Override
    public String getUsername() {
        return this.email;
    }


    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }


}
