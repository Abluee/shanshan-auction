package com.shanshan.auction.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("item_image")
public class ItemImage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long itemId;
    private String url;
    private Integer sort;
    private LocalDateTime createdAt;
} 