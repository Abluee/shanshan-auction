package com.shanshan.auction.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private String role;
    private String token;
} 