package com.shanshan.auction.controller;

import com.shanshan.auction.common.Result;
import com.shanshan.auction.dto.LoginRequest;
import com.shanshan.auction.dto.LoginResponse;
import com.shanshan.auction.service.UserService;
import com.shanshan.auction.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final JwtUtil jwtUtils;

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        
        return Result.success(response);
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        // 可以在这里处理token黑名单等逻辑
        return Result.success();
    }
} 