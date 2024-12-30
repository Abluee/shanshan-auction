package com.shanshan.auction.controller;

import com.shanshan.auction.common.Result;
import com.shanshan.auction.dto.BidHistoryResponse;
import com.shanshan.auction.model.Item;
import com.shanshan.auction.service.BidService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/bids")
@RequiredArgsConstructor
public class BidController {
    private final BidService bidService;

    @PostMapping("/place")
    public Result<Item> placeBid(
            @RequestParam @NotNull(message = "商品ID不能为空") Long itemId,
            @RequestParam @NotNull(message = "出价金额不能为空") @DecimalMin(value = "0.01", message = "出价金额必须大于0") BigDecimal price) {
        // 从 SecurityContext 中获取当前用户ID
        Long userId = getCurrentUserId();
        return Result.success(bidService.placeBid(itemId, userId, price));
    }

    private Long getCurrentUserId() {
        // 临时返回测试用户ID，实际应该从 Security Context 中获取
        return 1L;
    }

    @GetMapping("/history/{itemId}")
    public Result<List<BidHistoryResponse>> getBidHistory(@PathVariable Long itemId) {
        return Result.success(bidService.getBidHistory(itemId));
    }

    @GetMapping("/user/{userId}")
    public Result<List<BidHistoryResponse>> getUserBidHistory(@PathVariable Long userId) {
        return Result.success(bidService.getUserBidHistory(userId));
    }

    @GetMapping("/count/{itemId}")
    public Result<Integer> getBidCount(@PathVariable Long itemId) {
        return Result.success(bidService.getBidCount(itemId));
    }
} 