package com.example.userexample.common.loginAndLogout.handler;

import com.example.userexample.common.jwt.service.JwtService;
import com.example.userexample.services.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutHandler {

    private final JwtService jwtService;

    private final UserRepository userRepository;

    /**
     *  엑세스 토큰으로 검사 후
     *  토큰 정보와 일치하는 회원의 리프레쉬토큰 정보를 null로 update
     */
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        log.info("logout() 호출");

        jwtService.extractAccessToken(request).ifPresentOrElse(
                accessToken -> {
                    if (jwtService.isTokenValid(accessToken)) {
                        jwtService.extractEmail(accessToken)
                                .ifPresent(email -> userRepository.findByEmail(email)
                                        .ifPresentOrElse(
                                                user -> user.updateRefreshToken(null),
                                                () -> log.error("토큰 정보와 일치하는 회원이 없습니다.")
                                                )
                                );
                    } else {
                        throw new AccessDeniedException("접근이 거부되었습니다.");
                        //errorResponseWrite(response);
                    }
                }, () -> {
                    log.info("헤더에 accessToken이 없습니다!");
                    throw new AccessDeniedException("접근이 거부되었습니다.");
                    //errorResponseWrite(response);
                }

        );

    }

    private static void errorResponseWrite(HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/plain;charset=UTF-8");
        try {
            response.getWriter().write("accessToken을 확인해주세요");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
