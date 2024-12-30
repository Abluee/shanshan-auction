package com.shanshan.auction.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("bid")
public class Bid {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long itemId;
    private Long userId;
    private BigDecimal price;
    private LocalDateTime createdAt;
} 