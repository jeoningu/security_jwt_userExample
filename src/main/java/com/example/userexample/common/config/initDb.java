package com.example.userexample.common.config;

import com.example.userexample.services.user.model.RoleType;
import com.example.userexample.services.user.model.User;
import com.example.userexample.services.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;


@Component
@RequiredArgsConstructor
public class initDb {

    private final InitService initService;

    @PostConstruct
    public void init() {
        initService.dbInit();
    }

    @Component
    @Transactional
    @RequiredArgsConstructor
    static class InitService {

        private final PasswordEncoder passwordEncoder;
        private final UserRepository userRepository;

        public void dbInit() {
            User user = User.builder()
                    .email("admin@test.com")
                    .name("관리자")
                    .phoneNumber("000-0000-0000")
                    .role(RoleType.ADMIN)
                    .build();
            user.updatePasswordEncode("1234", passwordEncoder);
            userRepository.save(user);
        }
    }
}


