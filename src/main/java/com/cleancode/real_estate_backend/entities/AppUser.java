package com.cleancode.real_estate_backend.entities;



import com.cleancode.real_estate_backend.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppUser implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;


    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = false)
    private String email;

    @Column
    private String password;

    @Column
    private Boolean enabled = false;

    @Column(length = 6)
    private String verificationCode;

    @Column(length = 6)
    private String resetPasswordCode;

    @Temporal(TemporalType.TIMESTAMP)
    private Long createTs;

    @Column
    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<Role> role = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_representant_id")
    private Tenant tenantRepresentant;

//    @OneToMany(mappedBy = "creator", cascade = CascadeType.ALL, orphanRemoval = true)
//    private Set<Ticket> createdTickets = new HashSet<>();
//
//    @OneToMany(mappedBy = "responsibleManager", cascade = CascadeType.ALL, orphanRemoval = true)
//    private Set<Ticket> managedTickets = new HashSet<>();


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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppUser appUser = (AppUser) o;
        return Objects.equals(id, appUser.id) && Objects.equals(name, appUser.name) && Objects.equals(email, appUser.email) && Objects.equals(password, appUser.password) && Objects.equals(enabled, appUser.enabled) && Objects.equals(verificationCode, appUser.verificationCode) && Objects.equals(resetPasswordCode, appUser.resetPasswordCode) && Objects.equals(createTs, appUser.createTs) && Objects.equals(role, appUser.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, email, password, enabled, verificationCode, resetPasswordCode, createTs, role);
    }
}
