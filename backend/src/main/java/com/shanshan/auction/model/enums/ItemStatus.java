package com.shanshan.auction.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum ItemStatus {
    NOT_STARTED("NOT_STARTED", "未开始"),
    ONGOING("ONGOING", "进行中"),
    ENDED("ENDED", "已结束");

    @EnumValue
    private final String code;
    private final String desc;

    ItemStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
} 