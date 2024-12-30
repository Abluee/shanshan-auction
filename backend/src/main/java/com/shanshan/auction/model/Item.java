package com.shanshan.auction.model;

import com.baomidou.mybatisplus.annotation.*;
import com.shanshan.auction.model.enums.ItemStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("item")
public class Item {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String description;
    private BigDecimal startPrice;
    private BigDecimal currentPrice;
    private BigDecimal incrementAmount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer delayDuration;
    private ItemStatus status;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 
