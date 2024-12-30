package com.shanshan.auction.dto;

import com.shanshan.auction.model.Bid;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BidHistoryResponse {
    private Long id;
    private Long userId;
    private String username;
    private String userAvatar;
    private BigDecimal price;
    private LocalDateTime createdAt;

    public static BidHistoryResponse fromBid(Bid bid, String username, String userAvatar) {
        BidHistoryResponse response = new BidHistoryResponse();
        response.setId(bid.getId());
        response.setUserId(bid.getUserId());
        response.setUsername(username);
        response.setUserAvatar(userAvatar);
        response.setPrice(bid.getPrice());
        response.setCreatedAt(bid.getCreatedAt());
        return response;
    }
} 