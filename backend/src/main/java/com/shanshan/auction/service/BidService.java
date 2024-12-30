package com.shanshan.auction.service;

import com.shanshan.auction.dto.BidHistoryResponse;
import com.shanshan.auction.model.Item;
import java.math.BigDecimal;
import java.util.List;

public interface BidService {
    // 出价
    Item placeBid(Long itemId, Long userId, BigDecimal price);
    
    // 获取商品的出价历史
    List<BidHistoryResponse> getBidHistory(Long itemId);
    
    // 获取用户的出价历史
    List<BidHistoryResponse> getUserBidHistory(Long userId);
    
    // 获取商品的出价次数
    int getBidCount(Long itemId);
} 