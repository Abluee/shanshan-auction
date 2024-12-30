package com.shanshan.auction.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shanshan.auction.dto.LoginRequest;
import com.shanshan.auction.dto.LoginResponse;
import com.shanshan.auction.exception.BusinessException;
import com.shanshan.auction.mapper.UserMapper;
import com.shanshan.auction.model.User;
import com.shanshan.auction.service.UserService;
import com.shanshan.auction.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()));
        
        if (user == null || !user.getPassword().equals(request.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        LoginResponse response = new LoginResponse();
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setAvatar(user.getAvatar());
        response.setRole(user.getRole().getCode());
        response.setToken(jwtUtil.generateToken(user.getId()));
        
        return response;
    }

    @Override
    public User getById(Long userId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }
} 