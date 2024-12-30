package com.shanshan.auction.service;

import com.shanshan.auction.dto.LoginRequest;
import com.shanshan.auction.dto.LoginResponse;

public interface UserService {
    LoginResponse login(LoginRequest request);
} 