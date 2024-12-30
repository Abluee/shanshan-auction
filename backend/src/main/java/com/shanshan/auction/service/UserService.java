package com.shanshan.auction.service;

import com.shanshan.auction.dto.LoginRequest;
import com.shanshan.auction.dto.LoginResponse;
import com.shanshan.auction.model.User;

public interface UserService {
    LoginResponse login(LoginRequest request);

    User getById(Long userId);
}