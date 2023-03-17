package com.example.userexample.services.user.service;

import com.example.userexample.services.user.dto.AddUserDto;
import com.example.userexample.services.user.dto.ModifyUserDto;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface UserService {
    void addUser(AddUserDto addUserDto) throws Exception;
    void modifyUser(ModifyUserDto modifyUserDto) throws Exception;
    void removeUser() throws Exception;
    void reissue(HttpServletRequest request, HttpServletResponse response) throws Exception;
}
