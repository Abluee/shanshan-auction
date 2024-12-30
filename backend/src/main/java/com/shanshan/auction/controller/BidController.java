package com.shanshan.auction.controller;

import com.shanshan.auction.common.Result;
import com.shanshan.auction.dto.BidHistoryResponse;
import com.shanshan.auction.dto.BidRequest;
import com.shanshan.auction.model.Item;
import com.shanshan.auction.service.BidService;
import com.shanshan.auction.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/bids")
@RequiredArgsConstructor
public class BidController {
    private final BidService bidService;
    private final JwtUtil jwtUtils;

    @PostMapping("/place")
    public Result<Item> placeBid(
            @RequestBody BidRequest request,
            @RequestHeader("Authorization") String token) {
        Long userId = jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));
        return Result.success(bidService.placeBid(request.getItemId(), userId, request.getPrice()));
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