package com.shanshan.auction.dto;

import com.shanshan.auction.model.Item;
import com.shanshan.auction.model.ItemImage;
import com.shanshan.auction.model.enums.ItemStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ItemResponse {
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
    private String createdByName;
    private List<String> images;
    private List<BidHistoryResponse> bidHistory;
    private LocalDateTime createdAt;

    public static ItemResponse fromItem(Item item, List<ItemImage> images, String createdByName, List<BidHistoryResponse> bidHistory) {
        ItemResponse response = new ItemResponse();
        response.setId(item.getId());
        response.setTitle(item.getTitle());
        response.setDescription(item.getDescription());
        response.setStartPrice(item.getStartPrice());
        response.setCurrentPrice(item.getCurrentPrice());
        response.setIncrementAmount(item.getIncrementAmount());
        response.setStartTime(item.getStartTime());
        response.setEndTime(item.getEndTime());
        response.setDelayDuration(item.getDelayDuration());
        response.setStatus(item.getStatus());
        response.setCreatedBy(item.getCreatedBy());
        response.setCreatedByName(createdByName);
        response.setCreatedAt(item.getCreatedAt());
        response.setImages(images.stream()
                .sorted((a, b) -> a.getSort().compareTo(b.getSort()))
                .map(ItemImage::getUrl)
                .collect(Collectors.toList()));
        response.setBidHistory(bidHistory);
        return response;
    }

    public static ItemResponse fromItemBasic(Item item, List<ItemImage> images) {
        ItemResponse response = new ItemResponse();
        response.setId(item.getId());
        response.setTitle(item.getTitle());
        response.setDescription(item.getDescription());
        response.setStartPrice(item.getStartPrice());
        response.setCurrentPrice(item.getCurrentPrice());
        response.setIncrementAmount(item.getIncrementAmount());
        response.setStartTime(item.getStartTime());
        response.setEndTime(item.getEndTime());
        response.setDelayDuration(item.getDelayDuration());
        response.setStatus(item.getStatus());
        response.setImages(images.stream()
                .map(ItemImage::getUrl)
                .collect(Collectors.toList()));
        return response;
    }
} 