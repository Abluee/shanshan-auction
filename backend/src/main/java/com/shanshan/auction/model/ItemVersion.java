package com.shanshan.auction.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("item_version")
public class ItemVersion {
    @TableId
    private Long itemId;
    private Long version;
    private BigDecimal currentPrice;
    private LocalDateTime updatedAt;
} 