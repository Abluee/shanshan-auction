package com.shanshan.auction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shanshan.auction.model.Bid;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BidMapper extends BaseMapper<Bid> {
    @Select("SELECT * FROM bid WHERE item_id = #{itemId} ORDER BY price DESC, created_at DESC")
    List<Bid> findByItemId(@Param("itemId") Long itemId);

    @Select("<script>" +
            "SELECT * FROM bid WHERE item_id IN " +
            "<foreach collection='itemIds' item='itemId' open='(' separator=',' close=')'>" +
            "#{itemId}" +
            "</foreach>" +
            " ORDER BY price DESC, created_at DESC" +
            "</script>")
    List<Bid> findByItemIds(@Param("itemIds") List<Long> itemIds);

    @Select("SELECT * FROM bid WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<Bid> findByUserId(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM bid WHERE item_id = #{itemId}")
    int countByItemId(@Param("itemId") Long itemId);
} 