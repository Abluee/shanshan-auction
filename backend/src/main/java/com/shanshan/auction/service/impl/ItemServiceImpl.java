package com.shanshan.auction.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shanshan.auction.dto.BidHistoryResponse;
import com.shanshan.auction.dto.CreateItemRequest;
import com.shanshan.auction.dto.ItemRequest;
import com.shanshan.auction.dto.ItemResponse;
import com.shanshan.auction.exception.BusinessException;
import com.shanshan.auction.mapper.BidMapper;
import com.shanshan.auction.mapper.ItemImageMapper;
import com.shanshan.auction.mapper.ItemMapper;
import com.shanshan.auction.mapper.UserMapper;
import com.shanshan.auction.model.Bid;
import com.shanshan.auction.model.Item;
import com.shanshan.auction.model.ItemImage;
import com.shanshan.auction.model.User;
import com.shanshan.auction.model.enums.ItemStatus;
import com.shanshan.auction.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemMapper itemMapper;
    private final ItemImageMapper itemImageMapper;
    private final BidMapper bidMapper;
    private final UserMapper userMapper;

    private ItemStatus calculateItemStatus(Item item) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(item.getStartTime())) {
            return ItemStatus.NOT_STARTED;
        } else if (now.isAfter(item.getEndTime())) {
            return ItemStatus.ENDED;
        } else {
            return ItemStatus.ONGOING;
        }
    }

    @Override
    public List<ItemResponse> listAll() {
        // 1. 获取所有商品
        List<Item> items = itemMapper.selectList(null);
        if (items.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 获取所有商品ID
        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .collect(Collectors.toList());

        // 3. 批量获取商品图片
        List<ItemImage> allImages = itemImageMapper.findByItemIds(itemIds);
        Map<Long, List<ItemImage>> imageMap = allImages.stream()
                .collect(Collectors.groupingBy(ItemImage::getItemId));

        // 4. 批量获取出价记录
        List<Bid> allBids = bidMapper.findByItemIds(itemIds);
        Map<Long, List<Bid>> bidMap = allBids.stream()
                .collect(Collectors.groupingBy(Bid::getItemId));

        // 5. 获取所有相关用户ID（包括创建者和出价者）
        Set<Long> userIds = new HashSet<>();
        // 添加商品创建者ID
        userIds.addAll(items.stream()
                .map(Item::getCreatedBy)
                .collect(Collectors.toList()));
        // 添加出价者ID
        userIds.addAll(allBids.stream()
                .map(Bid::getUserId)
                .collect(Collectors.toList()));

        // 6. 批量获取用户信息
        List<User> users = userMapper.selectBatchIds(userIds);
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        // 7. 组装响应数据
        return items.stream()
                .map(item -> {
                    // 更新状态
                    item.setStatus(calculateItemStatus(item));
                    
                    // 获取商品图片
                    List<ItemImage> images = imageMap.getOrDefault(item.getId(), new ArrayList<>());
                    
                    // 获取出价记录
                    List<Bid> bids = bidMap.getOrDefault(item.getId(), new ArrayList<>());
                    
                    // 获取创建者信息
                    User creator = userMap.get(item.getCreatedBy());
                    String creatorName = creator != null ? creator.getNickname() : "未知用户";

                    // 转换出价历史
                    List<BidHistoryResponse> bidHistory = bids.stream()
                            .map(bid -> {
                                User bidder = userMap.get(bid.getUserId());
                                return BidHistoryResponse.fromBid(
                                        bid,
                                        bidder != null ? bidder.getNickname() : "未知用户",
                                        bidder != null ? bidder.getAvatar() : null
                                );
                            })
                            .sorted(Comparator.comparing(BidHistoryResponse::getCreatedAt).reversed())
                            .collect(Collectors.toList());

                    return ItemResponse.fromItem(item, images, creatorName, bidHistory);
                })
                .collect(Collectors.toList());
    }

    @Override
    public ItemResponse getById(Long id) {
        Item item = itemMapper.selectById(id);
        if (item == null) {
            throw new RuntimeException("商品不存在");
        }

        // 更新状态
        item.setStatus(calculateItemStatus(item));
        
        List<ItemImage> images = itemImageMapper.selectList(
            new QueryWrapper<ItemImage>()
                .eq("item_id", id)
                .orderByAsc("sort")
        );

        List<Bid> bids = bidMapper.findByItemId(id);
        User creator = userMapper.selectById(item.getCreatedBy());

        List<BidHistoryResponse> bidHistory = bids.stream()
            .map(bid -> {
                User bidder = userMapper.selectById(bid.getUserId());
                return BidHistoryResponse.fromBid(
                    bid,
                    bidder != null ? bidder.getNickname() : "未知用户",
                    bidder != null ? bidder.getAvatar() : null
                );
            })
            .collect(Collectors.toList());

        return ItemResponse.fromItem(
            item,
            images,
            creator != null ? creator.getNickname() : "未知用户",
            bidHistory
        );
    }


    @Override
    @Transactional
    public ItemResponse save(ItemRequest request) {
        // 1. 创建商品
        Item item = new Item();
        item.setTitle(request.getTitle());
        item.setDescription(request.getDescription());
        item.setStartPrice(request.getStartPrice());
        item.setCurrentPrice(request.getStartPrice());
        item.setIncrementAmount(request.getIncrementAmount());
        item.setStartTime(request.getStartTime());
        item.setEndTime(request.getEndTime());
        item.setDelayDuration(request.getDelayDuration());
        item.setStatus(ItemStatus.NOT_STARTED);
        item.setCreatedBy(getCurrentUserId());
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());

        itemMapper.insert(item);

        // 2. 初始化版本号
        itemMapper.initVersion(item.getId());

        // 3. 保存商品图片
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            int sort = 0;
            for (String url : request.getImageUrls()) {
                ItemImage image = new ItemImage();
                image.setItemId(item.getId());
                image.setUrl(url);
                image.setSort(sort++);
                image.setCreatedAt(LocalDateTime.now());
                itemImageMapper.insert(image);
            }
        }

        return getById(item.getId());
    }

    public ItemResponse update(Long id, ItemRequest request) {
        Item item = itemMapper.selectById(id);
        if (item == null) {
            throw new RuntimeException("商品不存在");
        }

        // 1. 更新商品基本信息
        item.setTitle(request.getTitle());
        item.setDescription(request.getDescription());
        item.setStartPrice(request.getStartPrice());
        item.setIncrementAmount(request.getIncrementAmount());
        item.setStartTime(request.getStartTime());
        item.setEndTime(request.getEndTime());
        item.setDelayDuration(request.getDelayDuration());
        item.setUpdatedAt(LocalDateTime.now());

        itemMapper.updateById(item);

        // 2. 更新商品图片
        // 先删除原有图片
        itemImageMapper.delete(
            new QueryWrapper<ItemImage>().eq("item_id", id)
        );

        // 重新添加图片
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            int sort = 0;
            for (String url : request.getImageUrls()) {
                ItemImage image = new ItemImage();
                image.setItemId(id);
                image.setUrl(url);
                image.setSort(sort++);
                image.setCreatedAt(LocalDateTime.now());
                itemImageMapper.insert(image);
            }
        }

        return getById(id);
    }

    @Override
    @Transactional
    public void remove(Long id) {
        // 1. 检查商品状态
        Item item = itemMapper.selectById(id);
        if (item == null) {
            throw new BusinessException("商品不存在");
        }
        if (item.getStatus() == ItemStatus.ONGOING) {
            throw new BusinessException("拍卖进行中的商品不能删除");
        }

        // 2. 删除商品图片
        itemImageMapper.delete(new QueryWrapper<ItemImage>().eq("item_id", id));

        // 3. 删除版本记录
        itemMapper.deleteVersion(id);

        // 4. 删除商品
        itemMapper.deleteById(id);
    }

    private Long getCurrentUserId() {
        // 从请求上下文中获取当前用户ID
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new BusinessException("无法获取当前用户信息");
        }
        
        Long userId = (Long) attributes.getRequest().getAttribute("userId");
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }
        
        return userId;
    }

    @Override
    public List<ItemResponse> listBasic() {
        // 1. 获取所有商品
        List<Item> items = itemMapper.selectList(null);
        if (items.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 获取所有商品ID
        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .collect(Collectors.toList());

        // 3. 批量获取商品图片
        List<ItemImage> allImages = itemImageMapper.findByItemIds(itemIds);
        Map<Long, List<ItemImage>> imageMap = allImages.stream()
                .collect(Collectors.groupingBy(ItemImage::getItemId));

        // 4. 组装响应数据（不包含出价历史）
        return items.stream()
                .map(item -> {
                    // 更新状态
                    item.setStatus(calculateItemStatus(item));
                    
                    // 获取商品图片
                    List<ItemImage> images = imageMap.getOrDefault(item.getId(), new ArrayList<>());
                    
                    return ItemResponse.fromItemBasic(item, images);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public Item createItem(CreateItemRequest request) {
        // ... 创建商品的其他逻辑 ...

        // 保存商品信息
        Item item = convertToItem(request);
        itemMapper.insert(item);

        // 初始化版本号
        itemMapper.initVersion(item.getId());

        // ... 保存图片等其他逻辑 ...

        return item;
    }

    private Item convertToItem(CreateItemRequest request) {
        Item item = new Item();
        item.setTitle(request.getTitle());
        item.setDescription(request.getDescription());
        item.setStartPrice(request.getStartPrice());
        item.setCurrentPrice(request.getStartPrice()); // 初始当前价格等于起拍价
        item.setIncrementAmount(request.getIncrementAmount());
        item.setStartTime(request.getStartTime());
        item.setEndTime(request.getEndTime());
        item.setDelayDuration(request.getDelayDuration());
        item.setStatus(calculateItemStatus(item)); // 根据时间计算状态
        item.setCreatedBy(getCurrentUserId());
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        return item;
    }
} 