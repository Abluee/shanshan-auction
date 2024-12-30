package com.shanshan.auction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shanshan.auction.model.Item;
import com.shanshan.auction.model.Bid;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface ItemMapper extends BaseMapper<Item> {
    @Select("SELECT * FROM item WHERE id = #{id}")
    Item selectById(@Param("id") Long id);
    
    @Update("UPDATE item i, item_version v SET " +
            "i.current_price = #{price}, " +
            "i.updated_at = NOW(), " +
            "v.current_price = #{price}, " +
            "v.version = v.version + 1, " +
            "v.updated_at = NOW() " +
            "WHERE i.id = #{id} " +
            "AND v.item_id = i.id " +
            "AND v.version = #{version} " +
            "AND #{price} >= i.current_price + i.increment_amount")
    int updatePriceWithVersion(@Param("id") Long id, 
                             @Param("price") BigDecimal price, 
                             @Param("version") Long version);

    @Select("SELECT version FROM item_version WHERE item_id = #{itemId}")
    Long getVersion(@Param("itemId") Long itemId);

    @Select("SELECT * FROM bid WHERE item_id IN " +
            "<foreach collection='itemIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " ORDER BY created_at DESC")
    List<Bid> findByItemIds(@Param("itemIds") List<Long> itemIds);
} 