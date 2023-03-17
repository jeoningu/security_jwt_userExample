package com.example.userexample.services.user.service.Impl;

import com.example.userexample.common.jwt.service.JwtService;
import com.example.userexample.common.util.SecurityUtil;
import com.example.userexample.services.user.dto.AddUserDto;
import com.example.userexample.services.user.dto.ModifyUserDto;
import com.example.userexample.services.user.model.RoleType;
import com.example.userexample.services.user.model.User;
import com.example.userexample.services.user.repository.UserRepository;
import com.example.userexample.services.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    @Transactional
    public void addUser(AddUserDto addUserDto) throws Exception {

        if (userRepository.findByEmail(addUserDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        User user = User.builder()
                .email(addUserDto.getEmail())
//                .password(addUserDto.getPassword())
                .name(addUserDto.getName())
                .phoneNumber(addUserDto.getPhoneNumber())
                .role(RoleType.USER)
                .build();
        user.updatePasswordEncode(addUserDto.getPassword(), passwordEncoder);

        userRepository.save(user);
    }

    @Transactional
    public void modifyUser(ModifyUserDto modifyUserDto) throws Exception {
        User user = userRepository
                .findByEmail(SecurityUtil.getLoginUserEmail())
                .orElseThrow(() -> new RuntimeException("로그인 유저 정보가 없습니다."));

        if (StringUtils.hasText(modifyUserDto.getPassword())) {
            user.updatePasswordEncode(modifyUserDto.getPassword(), passwordEncoder);
        }
        if (StringUtils.hasText(modifyUserDto.getName())) {
            user.updateName(modifyUserDto.getName());
        }
        if (StringUtils.hasText(modifyUserDto.getPhoneNumber())) {
            user.updatePhoneNumber(modifyUserDto.getPhoneNumber());
        }
    }

    @Transactional
    public void removeUser() throws Exception {
        User user = userRepository
                .findByEmail(SecurityUtil.getLoginUserEmail())
                .orElseThrow(() -> new RuntimeException("로그인 유저 정보가 없습니다."));

        userRepository.delete(user);
    }

    @Transactional
    public void reissue(HttpServletRequest request, HttpServletResponse response) throws Exception {

        // 리프레시 토큰 검증
        String refreshToken = jwtService.extractRefreshToken(request)
                .filter(jwtService::isTokenValid)
                .orElse(null);

        if (refreshToken != null) {
            userRepository.findByRefreshToken(refreshToken)
                    .ifPresentOrElse(user -> {
                                // 리프레시 토큰 재발급 후 db에 반영
                                String reIssuedRefreshToken = jwtService.createRefreshToken();
                                user.updateRefreshToken(reIssuedRefreshToken);

                                // 헤더에 엑세스 토큰, 리프레시 토큰 반영
                                jwtService.sendAccessAndRefreshToken(response,
                                        jwtService.createAccessToken(user.getEmail()),
                                        reIssuedRefreshToken);
                            },
                            () -> {
                                throw new IllegalArgumentException("리프레시 토큰 정보와 일치하는 회원이 없습니다.");
                            }
                    );
        } else {
            throw new IllegalArgumentException("리프레시 토큰 정보가 유효하지 않습니다.");
        }
    }


}
