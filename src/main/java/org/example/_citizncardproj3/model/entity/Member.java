package org.example._citizncardproj3.model.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "Members")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MemberID")
    private Long memberId;

    @Column(name = "Email", unique = true, nullable = false)
    private String email;

    @Column(name = "Password", nullable = false)
    private String password;

    @Column(name = "Phone", unique = true)
    private String phone;

    @Column(name = "Name", nullable = false)
    private String name;

    @Column(name = "Birthday")
    private LocalDate birthday;

    @Enumerated(EnumType.STRING)
    @Column(name = "Gender")
    private Gender gender;

    @Column(name = "Address")
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "Role", nullable = false)
    private Role role;

    @Column(name = "RegisterDate", nullable = false)
    private LocalDateTime registerDate;

    @Column(name = "LastLoginTime")
    private LocalDateTime lastLoginTime;

    @Column(name = "LastPasswordChange")
    private LocalDateTime lastPasswordChange;

    @Column(name = "FailedLoginAttempts")
    private Integer failedLoginAttempts;

    @Column(name = "AccountLocked")
    private Boolean accountLocked;

    @Column(name = "PasswordResetToken")
    private String passwordResetToken;

    @Column(name = "PasswordResetExpiry")
    private LocalDateTime passwordResetExpiry;

    @CreationTimestamp
    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "IsDeleted", nullable = false)
    private Boolean isDeleted;

    @Column(name = "DeletedAt")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<CitizenCard> citizenCards;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Booking> bookings;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<DiscountUsage> discountUsages;

    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL)
    private Wallet wallet;

    // 性別枚舉
    @Getter
    public enum Gender {
        MALE("男"),
        FEMALE("女"),
        OTHER("其他");

        private final String description;

        Gender(String description) {
            this.description = description;
        }

    }

    // 角色枚舉
    @Getter
    public enum Role {
        ROLE_USER("一般用戶"),
        ROLE_ADMIN("管理員");

        private final String description;

        Role(String description) {
            this.description = description;
        }

    }

    // 初始化方法
    @PrePersist
    public void prePersist() {
        if (this.isDeleted == null) {
            this.isDeleted = false;
        }
        if (this.role == null) {
            this.role = Role.ROLE_USER;
        }
        if (this.failedLoginAttempts == null) {
            this.failedLoginAttempts = 0;
        }
        if (this.accountLocked == null) {
            this.accountLocked = false;
        }
        if (this.registerDate == null) {
            this.registerDate = LocalDateTime.now();
        }
    }

    // 業務方法
    public void recordLoginSuccess() {
        this.lastLoginTime = LocalDateTime.now();
        this.failedLoginAttempts = 0;
        this.accountLocked = false;
    }

    public void recordLoginFailure() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= 5) {
            this.accountLocked = true;
        }
    }

    public void resetPassword(String newPassword) {
        this.password = newPassword;
        this.lastPasswordChange = LocalDateTime.now();
        this.passwordResetToken = null;
        this.passwordResetExpiry = null;
    }

    public void updateProfile(String name, String phone, String address) {
        this.name = name;
        this.phone = phone;
        this.address = address;
    }

    public int getAge() {
        if (birthday == null) {
            return 0;
        }
        return Period.between(birthday, LocalDate.now()).getYears();
    }

    public void softDelete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    // 實現UserDetails接口的方法
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(role.name()));
        return authorities;
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
        return !this.accountLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        if (lastPasswordChange == null) {
            return true;
        }
        return lastPasswordChange.plusDays(90).isAfter(LocalDateTime.now());
    }

    @Override
    public boolean isEnabled() {
        return !this.isDeleted;
    }

    @Override
    public String toString() {
        return String.format("Member{id=%d, email='%s', name='%s'}",
                memberId, email, name);
    }
}