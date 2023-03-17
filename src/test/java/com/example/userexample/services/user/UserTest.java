package com.example.userexample.services.user;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.userexample.services.user.dto.AddUserDto;
import com.example.userexample.services.user.model.User;
import com.example.userexample.services.user.repository.UserRepository;
import com.example.userexample.services.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@RunWith(SpringRunner.class)

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Slf4j
public class UserTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Value("${jwt.access.header}")
    private String accessHeader;

    @Value("${jwt.refresh.header}")
    private String refreshHeader;

    @Value("${jwt.secretKey}")
    private String secretKey;

    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String EMAIL = "test1@test.com";
    private static final String PASSWORD = "password1";
    private static final String ACCESS_TOKEN_SUBJECT = "AccessToken";
    private static final String REFRESH_TOKEN_SUBJECT = "RefreshToken";
    private static final String BEARER = "Bearer ";

    @BeforeEach
    private void init() throws Exception {
        AddUserDto addUserDto = AddUserDto.builder()
                .email(EMAIL)
                .password(PASSWORD)
                .name("테스트1")
                .phoneNumber("010-123-123")
                .build();
        userService.addUser(addUserDto);
    }


    @Test
    @DisplayName("회원 가입 테스트")
    void addUser_Test() throws Exception {
        Map<String, String> signUser = new HashMap<>();
        signUser.put("email", "test2@test.com");
        signUser.put("password", "password2");
        signUser.put("name", "테스트2");
        signUser.put("phoneNumber", "010-1234-1234");

        mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUser)))
                .andExpect(status().isOk());

        Optional<User> byEmail = userRepository.findByEmail("test2@test.com");
        assertEquals(byEmail.get().getName(), "테스트2");
        assertEquals(byEmail.get().getPhoneNumber(), "010-1234-1234");
    }

    @Test
    @DisplayName("회원 가입-이메일 중복 테스트")
    void userAdd_emailDuplicate_test() throws Exception {
        Map<String, String> signUser = new HashMap<>();
        signUser.put("email", EMAIL);
        signUser.put("password", PASSWORD);
        signUser.put("name", "테스트1");
        signUser.put("phoneNumber", "010-1234-1234");
        Assertions.assertThatThrownBy(
                        () -> mockMvc.perform(post("/user")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(signUser)))
                ).hasCauseInstanceOf(IllegalArgumentException.class).hasMessageContaining("이미 존재하는 이메일입니다.");
    }

    @Test
    @DisplayName("로그인 테스트")
    public void login_Test() throws Exception {
        Map<String, String> usernamePasswordMap = getUsernamePasswordMap(EMAIL, PASSWORD);

        // POST "/login", application/json, content로 이메일, 패스워드 Map 요청 결과 반환
        MvcResult result = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usernamePasswordMap)))
                .andExpect(status().isOk())
                .andReturn();

        String accessToken = result.getResponse().getHeader(accessHeader);
        String refreshToken = result.getResponse().getHeader(refreshHeader);

        String accessTokenSubject = JWT.require(Algorithm.HMAC512(secretKey)).build()
                .verify(accessToken).getSubject();
        String refreshTokenSubject = JWT.require(Algorithm.HMAC512(secretKey)).build()
                .verify(refreshToken).getSubject();

        assertThat(accessTokenSubject).isEqualTo(ACCESS_TOKEN_SUBJECT);
        assertThat(refreshTokenSubject).isEqualTo(REFRESH_TOKEN_SUBJECT);
    }

    /**
     * 로그인 요청을 보내서 액세스 토큰, 리프레시 토큰을 Map에 담아 반환
     */
    private Map<String, String> getLoginTokenMap() throws Exception {

        Map<String, String> usernamePasswordMap = getUsernamePasswordMap(EMAIL, PASSWORD);

        // POST "/login", application/json, content로 이메일, 패스워드 Map 요청 결과 반환
        MvcResult result = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usernamePasswordMap)))
                .andReturn();

        String accessToken = result.getResponse().getHeader(accessHeader);
        String refreshToken = result.getResponse().getHeader(refreshHeader);

        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put(accessHeader, accessToken);
        tokenMap.put(refreshHeader, refreshToken);
        tokenMap.put(KEY_EMAIL, usernamePasswordMap.get(KEY_EMAIL));
        return tokenMap;
    }

    /**
     * Key : email, password인 usernamePasswordMap 반환
     */
    private Map<String, String> getUsernamePasswordMap(String email, String password) {
        Map<String, String> usernamePasswordMap = new LinkedHashMap<>();
        usernamePasswordMap.put(KEY_EMAIL, email);
        usernamePasswordMap.put(KEY_PASSWORD, password);
        return usernamePasswordMap;
    }


    @Test
    @DisplayName("회원 정보 수정 테스트")
    void modifyUser_Test() throws Exception {
        // given
        Map<String, String> tokenMap = getLoginTokenMap();
        String accessToken = tokenMap.get(accessHeader);
        String email = tokenMap.get(KEY_EMAIL);

        Map<String, String> modifyUser = new HashMap<>();
        modifyUser.put("password", "password3");
        modifyUser.put("name", "테스트3");
        modifyUser.put("phoneNumber", "010-3333-3333");

        // when, then
        mockMvc.perform(put("/user/me") // "/login"이 아니고, 존재하는 주소를 보내기
                        .header(accessHeader, BEARER + accessToken) // 유효한 AccessToken만 담아서 요청
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(modifyUser))
                ).andExpect(status().isOk());

        User user = userRepository.findByEmail(email).get();
        assertEquals(user.getName(), "테스트3");
        assertEquals(user.getPhoneNumber(), "010-3333-3333");
    }

    @Test
    @DisplayName("회원 탈퇴 테스트")
    void removeUser_Test() throws Exception {
        // given
        Map<String, String> tokenMap = getLoginTokenMap();
        String accessToken = tokenMap.get(accessHeader);
        String email = tokenMap.get(KEY_EMAIL);

        // when, then
        mockMvc.perform(delete("/user/me") // "/login"이 아니고, 존재하는 주소를 보내기
                .header(accessHeader, BEARER + accessToken) // 유효한 AccessToken만 담아서 요청
        ).andExpect(status().isOk());

        Optional<User> byEmail = userRepository.findByEmail(email);
        assertEquals(byEmail, Optional.empty());
    }

    @Test
    @DisplayName("로그아웃 테스트")
    void logout_test() throws Exception {
        // given
        Map<String, String> tokenMap = getLoginTokenMap();
        String accessToken = tokenMap.get(accessHeader);
        String refreshToken = tokenMap.get(refreshHeader);

        // when, then
        mockMvc.perform(post("/logout") //
                .header(accessHeader, BEARER + accessToken) // 유효한 AccessToken만 담아서 요청
        ).andExpect(status().isOk());

        Optional<User> byRefreshToken = userRepository.findByRefreshToken(refreshToken);
        assertEquals(byRefreshToken, Optional.empty());
    }

    @Test
    @DisplayName("로그아웃 테스트-토큰 없이 테스트")
    void logout_notToken_test() throws Exception {
        Assertions.assertThatThrownBy(
                () -> mockMvc.perform(post("/logout"))
        ).isInstanceOf(AccessDeniedException.class).hasMessageContaining("접근이 거부되었습니다.");
    }

    @Test
    @DisplayName("리프레시 토큰을 이용해서 재발급")
    void reissue_test() throws Exception {
        // given
        Map<String, String> tokenMap = getLoginTokenMap();
        String refreshToken = tokenMap.get(refreshHeader);

        MvcResult result = mockMvc.perform(post("/user/reissue")
                        .header(refreshHeader, BEARER + refreshToken)
                )
                .andExpect(status().isOk())
                .andReturn();

        String accessToken = result.getResponse().getHeader(accessHeader);
        String reIssuedRefreshToken = result.getResponse().getHeader(refreshHeader);

        String accessTokenSubject = JWT.require(Algorithm.HMAC512(secretKey)).build()
                .verify(accessToken).getSubject();
        String refreshTokenSubject = JWT.require(Algorithm.HMAC512(secretKey)).build()
                .verify(reIssuedRefreshToken).getSubject();

        assertThat(accessTokenSubject).isEqualTo(ACCESS_TOKEN_SUBJECT);
        assertThat(refreshTokenSubject).isEqualTo(REFRESH_TOKEN_SUBJECT);
    }



}
