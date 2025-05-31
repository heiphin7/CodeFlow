package com.api.codeflow.dto;

import lombok.Data;

@Data
public class UpdateUserInfoDto {
    private String username;
    private String email;
    private String preferredLanguage;
    private String githubLink;
    private String location;
}
