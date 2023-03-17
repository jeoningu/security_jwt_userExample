package com.example.userexample.services.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Pattern;

@NoArgsConstructor
@Getter
@Builder
@AllArgsConstructor
public class ModifyUserDto {

    private String password;

    private String name;

    @Pattern(regexp = "^[0-9]{3}-[0-9]{3,4}-[0-9]{4}$", message = "핸드폰번호의 형식이 올바르지 않습니다.")
    private String phoneNumber;
}
