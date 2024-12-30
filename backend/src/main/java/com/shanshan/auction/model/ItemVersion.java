package com.shanshan.auction.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("item_version")
public class ItemVersion {
    @TableId(type = IdType.INPUT)
    private Long itemId;
    private Long version;
} 