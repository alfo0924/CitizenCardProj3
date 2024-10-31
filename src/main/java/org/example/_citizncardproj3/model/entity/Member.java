package org.example._citizncardproj3.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
@Table(name = "members")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String phone;

    private String address;

    private LocalDate birthday;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    private MemberStatus status;

    @Enumerated(EnumType.STRING)
    private Role role;

    private Integer failedLoginAttempts;

    private LocalDateTime lastLoginTime;

    private LocalDateTime lastPasswordChangeTime;

    private String passwordResetToken;

    private LocalDateTime passwordResetExpiry;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<CitizenCard> citizenCards;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Booking> bookings;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<DiscountUsage> discountUsages;

    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL)
    private Wallet wallet;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Boolean isDeleted;

    // 性別枚舉
    public enum Gender {
        MALE("男"),
        FEMALE("女"),
        OTHER("其他");

        private final String description;

        Gender(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 會員狀態枚舉
    public enum MemberStatus {
        ACTIVE("正常"),
        INACTIVE("未啟用"),
        SUSPENDED("已停權"),
        LOCKED("已鎖定");

        private final String description;

        MemberStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 角色枚舉
    public enum Role {
        ROLE_USER,
        ROLE_ADMIN
    }

    // 初始化方法
    @PrePersist
    public void prePersist() {
        if (this.isDeleted == null) {
            this.isDeleted = false;
        }
        if (this.status == null) {
            this.status = MemberStatus.INACTIVE;
        }
        if (this.role == null) {
            this.role = Role.ROLE_USER;
        }
        if (this.failedLoginAttempts == null) {
            this.failedLoginAttempts = 0;
        }
    }

    // 業務方法

    // 啟用帳號
    public void activate() {
        if (this.status == MemberStatus.INACTIVE) {
            this.status = MemberStatus.ACTIVE;
        } else {
            throw new IllegalStateException("只有未啟用的帳號可以啟用");
        }
    }

    // 停權帳號
    public void suspend(String reason) {
        if (this.status == MemberStatus.ACTIVE) {
            this.status = MemberStatus.SUSPENDED;
        } else {
            throw new IllegalStateException("只有正常狀態的帳號可以停權");
        }
    }

    // 解鎖帳號
    public void unlock() {
        if (this.status == MemberStatus.LOCKED) {
            this.status = MemberStatus.ACTIVE;
            this.failedLoginAttempts = 0;
        }
    }

    // 記錄登入失敗
    public void recordLoginFailure() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= 5) {
            this.status = MemberStatus.LOCKED;
        }
    }

    // 重設密碼
    public void resetPassword(String newPassword) {
        this.password = newPassword;
        this.lastPasswordChangeTime = LocalDateTime.now();
        this.passwordResetToken = null;
        this.passwordResetExpiry = null;
    }

    // 更新個人資料
    public void updateProfile(String name, String phone, String address) {
        this.name = name;
        this.phone = phone;
        this.address = address;
    }

    // 計算年齡
    public int getAge() {
        if (birthday == null) {
            return 0;
        }
        return Period.between(birthday, LocalDate.now()).getYears();
    }

    // 檢查帳號是否有效
    public boolean isAccountValid() {
        return status == MemberStatus.ACTIVE && !isDeleted;
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
        return status != MemberStatus.LOCKED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        if (lastPasswordChangeTime == null) {
            return true;
        }
        // 密碼90天過期
        return lastPasswordChangeTime.plusDays(90).isAfter(LocalDateTime.now());
    }

    @Override
    public boolean isEnabled() {
        return status == MemberStatus.ACTIVE && !isDeleted;
    }

    // 用於日誌記錄的方法
    public String toLogString() {
        return String.format("Member{id=%d, email='%s', name='%s', status=%s}",
                memberId, email, name, status);
    }
}