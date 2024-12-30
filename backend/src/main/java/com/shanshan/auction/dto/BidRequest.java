package com.shanshan.auction.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class BidRequest {
    @NotNull(message = "商品ID不能为空")
    private Long itemId;

    @NotNull(message = "出价金额不能为空")
    @DecimalMin(value = "0.01", message = "出价金额必须大于0")
    private BigDecimal price;
} 