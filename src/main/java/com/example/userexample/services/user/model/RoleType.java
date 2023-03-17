package com.example.userexample.services.user.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RoleType {

    ADMIN("ROLE_ADMIN"),
    USER("ROLE_USER");

    private String key;
}
