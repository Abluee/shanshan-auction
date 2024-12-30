package com.shanshan.auction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shanshan.auction.model.ItemImage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ItemImageMapper extends BaseMapper<ItemImage> {
    @Select("<script>" +
            "SELECT * FROM item_image WHERE item_id IN " +
            "<foreach collection='itemIds' item='itemId' open='(' separator=',' close=')'>" +
            "#{itemId}" +
            "</foreach>" +
            " ORDER BY sort ASC" +
            "</script>")
    List<ItemImage> findByItemIds(@Param("itemIds") List<Long> itemIds);
} 