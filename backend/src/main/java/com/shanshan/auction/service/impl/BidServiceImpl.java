package com.shanshan.auction.service.impl;

import com.shanshan.auction.dto.BidHistoryResponse;
import com.shanshan.auction.exception.BusinessException;
import com.shanshan.auction.mapper.BidMapper;
import com.shanshan.auction.mapper.ItemMapper;
import com.shanshan.auction.mapper.UserMapper;
import com.shanshan.auction.model.Bid;
import com.shanshan.auction.model.Item;
import com.shanshan.auction.model.User;
import com.shanshan.auction.model.enums.ItemStatus;
import com.shanshan.auction.service.BidService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BidServiceImpl implements BidService {
    private final ItemMapper itemMapper;
    private final BidMapper bidMapper;
    private final UserMapper userMapper;
    private static final int MAX_RETRY = 3;

    @Override
    @Transactional
    public Item placeBid(Long itemId, Long userId, BigDecimal price) {
        // 1. 获取商品信息
        Item item = itemMapper.selectById(itemId);
        if (item == null) {
            throw new BusinessException("商品不存在");
        }

        // 2. 验证商品状态
        validateItem(item, userId);

        // 3. 验证出价金额
        if (price.compareTo(item.getCurrentPrice().add(item.getIncrementAmount())) < 0) {
            throw new BusinessException("出价必须高于当前价格加上最小加价幅度");
        }

        // 4. 获取或初始化版本号
        Long version = itemMapper.getVersion(itemId);
        if (version == null) {
            itemMapper.initVersion(itemId,item.getCurrentPrice());
            version = 1L;
        }

        // 5. 使用乐观锁尝试更新
        int retryCount = 0;
        while (retryCount < MAX_RETRY) {
            // 更新价格
            int priceUpdated = itemMapper.updatePrice(itemId, price);
            if (priceUpdated > 0) {
                // 更新版本号
                int versionUpdated = itemMapper.updateVersion(itemId, version);
                if (versionUpdated > 0) {
                    // 创建出价记录
                    createBidRecord(itemId, userId, price);
                    
                    // 检查是否需要延长结束时间
                    LocalDateTime now = LocalDateTime.now();
                    if (item.getEndTime() != null && item.getDelayDuration() != null) {
                        long remainingSeconds = java.time.Duration.between(now, item.getEndTime()).getSeconds();
                        if (remainingSeconds < item.getDelayDuration()) {
                            // 延长结束时间
                            LocalDateTime newEndTime = now.plusSeconds(item.getDelayDuration());
                            itemMapper.updateEndTime(itemId, newEndTime);
                            item = itemMapper.selectById(itemId);
                        }
                    }
                    
                    return item;
                }
            }
            
            retryCount++;
            // 更新失败，重新获取商品信息和版本号
            item = itemMapper.selectById(itemId);
            version = itemMapper.getVersion(itemId);
            
            if (price.compareTo(item.getCurrentPrice().add(item.getIncrementAmount())) < 0) {
                throw new BusinessException("出价必须高于当前价格加上最小加价幅度");
            }
        }
        
        throw new BusinessException("出价失败，请重试");
    }

    @Override
    public List<BidHistoryResponse> getBidHistory(Long itemId) {
        List<Bid> bids = bidMapper.findByItemId(itemId);
        return convertToBidHistoryResponse(bids);
    }

    @Override
    public List<BidHistoryResponse> getUserBidHistory(Long userId) {
        List<Bid> bids = bidMapper.findByUserId(userId);
        return convertToBidHistoryResponse(bids);
    }

    @Override
    public int getBidCount(Long itemId) {
        return bidMapper.countByItemId(itemId);
    }

    private List<BidHistoryResponse> convertToBidHistoryResponse(List<Bid> bids) {
        // 如果没有出价记录，直接返回空列表
        if (bids == null || bids.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            // 获取所有相关用户ID
            List<Long> userIds = bids.stream()
                    .map(Bid::getUserId)
                    .distinct()
                    .collect(Collectors.toList());

            // 批量获取用户信息
            List<User> users = userMapper.selectBatchIds(userIds);
            Map<Long, User> userMap = users.stream()
                    .collect(Collectors.toMap(User::getId, user -> user, (k1, k2) -> k1));

            // 转换为响应对象
            return bids.stream()
                    .map(bid -> {
                        User user = userMap.getOrDefault(bid.getUserId(), null);
                        return BidHistoryResponse.fromBid(
                            bid,
                            user != null ? user.getNickname() : "未知用户",
                            user != null ? user.getAvatar() : null
                        );
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // 如果转换过程中出现任何错误，记录日志并返回空列表
            log.error("Convert bid history error", e);
            return new ArrayList<>();
        }
    }

    private void validateItem(Item item, Long userId) {
//        if (item.getStatus() != ItemStatus.ONGOING) {
//            throw new BusinessException("该商品不在拍卖中");
//        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(item.getStartTime())) {
            throw new BusinessException("拍卖还未开始");
        }
        if (now.isAfter(item.getEndTime())) {
            throw new BusinessException("拍卖已结束");
        }

//        if (userId.equals(item.getCreatedBy())) {
//            throw new BusinessException("不能对自己的商品出价");
//        }
    }

    private void createBidRecord(Long itemId, Long userId, BigDecimal price) {
        Bid bid = new Bid();
        bid.setItemId(itemId);
        bid.setUserId(userId);
        bid.setPrice(price);
        bid.setCreatedAt(LocalDateTime.now());
        bidMapper.insert(bid);
    }
} 