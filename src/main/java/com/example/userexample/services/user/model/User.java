package com.example.userexample.services.user.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String email; // 이메일

    private String password; // 비밀번호

    private String name; // 이름

    private String phoneNumber; // 전화번호

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleType role;      // 권한

    private String refreshToken; // 리프레시토큰

    public void updatePasswordEncode(String password, PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(password);
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updatePhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void updateRefreshToken(String updateRefreshToken) {
        this.refreshToken = updateRefreshToken;
    }

    @Builder // id 같이 생성전략이 db에 의존하는 경우 직접 값을 넘겨 받으면 안 되므로 @Builder, @AllArgsConstructor 대신 필요한 값만 받는 빌더 생성
    public User(String email/*, String password*/, String name, String phoneNumber, RoleType role) {
        this.email = email;
//        this.password = password;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }
}
