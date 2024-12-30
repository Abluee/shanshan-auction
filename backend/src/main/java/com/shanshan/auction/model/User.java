package com.shanshan.auction.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import com.shanshan.auction.model.enums.UserRole;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String avatar;
    private UserRole role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 