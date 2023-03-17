package com.example.userexample.services.user.controller;

import com.example.userexample.services.user.dto.AddUserDto;
import com.example.userexample.services.user.dto.ModifyUserDto;
import com.example.userexample.services.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @PostMapping
    public String addUser(@Valid @RequestBody AddUserDto addUserDto) throws Exception {
        userService.addUser(addUserDto);
        return "회원가입 성공";
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public String modifyUser(@Valid @RequestBody ModifyUserDto modifyUserDto) throws Exception {
        userService.modifyUser(modifyUserDto);
        return "내 회원정보수정 성공";
    }

    @DeleteMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public String removeUser() throws Exception {
        userService.removeUser();
        return "회원 탈퇴 성공";
    }

    @PostMapping("/reissue")
    public String reissue(HttpServletRequest request, HttpServletResponse response) throws Exception {
        userService.reissue(request, response);
        return "토큰 재발급 성공";
    }
}
