package com.shanshan.auction.controller;

import com.shanshan.auction.dto.ItemRequest;
import com.shanshan.auction.dto.ItemResponse;
import com.shanshan.auction.service.ItemService;
import com.shanshan.auction.common.Result;
import com.shanshan.auction.annotation.RequireAdmin;
import com.shanshan.auction.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Validated
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;
    private final JwtUtil jwtUtils;

    @GetMapping("/list")
    public Result<List<ItemResponse>> list() {
        return Result.success(itemService.listBasic());
    }

    @GetMapping("/{id}")
    public Result<ItemResponse> getById(@PathVariable @NotNull(message = "商品ID不能为空") Long id) {
        return Result.success(itemService.getById(id));
    }

    @PostMapping("/save")
    @RequireAdmin
    public Result<ItemResponse> save(@RequestBody @Valid ItemRequest request,@RequestHeader("Authorization") String token) {
        Long userId = jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));
        return Result.success(itemService.save(request,userId));
    }

    @PostMapping("/remove")
    @RequireAdmin
    public Result<Void> remove(@RequestParam @NotNull(message = "商品ID不能为空") Long id) {
        itemService.remove(id);
        return Result.success();
    }
} 