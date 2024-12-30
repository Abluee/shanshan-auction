package com.shanshan.auction.service;

import com.shanshan.auction.dto.ItemRequest;
import com.shanshan.auction.dto.ItemResponse;
import java.util.List;

public interface ItemService {
    List<ItemResponse> listBasic();
    List<ItemResponse> listAll();
    ItemResponse getById(Long id);
    ItemResponse save(ItemRequest request);  // 统一处理创建和更新
    void remove(Long id);
} 