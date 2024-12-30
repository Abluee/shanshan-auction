package com.shanshan.auction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shanshan.auction.model.Item;
import com.shanshan.auction.model.Bid;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Delete;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ItemMapper extends BaseMapper<Item> {
    @Select("SELECT * FROM item WHERE id = #{id}")
    Item selectById(@Param("id") Long id);
    
    @Update("UPDATE item SET current_price = #{price} WHERE id = #{itemId} AND current_price < #{price}")
    int updatePrice(@Param("itemId") Long itemId, @Param("price") BigDecimal price);

    @Select("SELECT version FROM item_version WHERE item_id = #{itemId}")
    Long getVersion(@Param("itemId") Long itemId);

    @Update("UPDATE item_version SET version = version + 1 WHERE item_id = #{itemId} AND version = #{version}")
    int updateVersion(@Param("itemId") Long itemId, @Param("version") Long version);

    @Insert("INSERT INTO item_version(item_id, version,current_price) VALUES(#{itemId}, 1,#{currentPrice})")
    int initVersion(@Param("itemId") Long itemId,@Param("currentPrice") BigDecimal currentPrice);

    @Select("SELECT * FROM bid WHERE item_id IN " +
            "<foreach collection='itemIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " ORDER BY created_at DESC")
    List<Bid> findByItemIds(@Param("itemIds") List<Long> itemIds);

    @Delete("DELETE FROM item_version WHERE item_id = #{itemId}")
    int deleteVersion(@Param("itemId") Long itemId);

    @Update("UPDATE item SET end_time = #{endTime} WHERE id = #{itemId}")
    int updateEndTime(@Param("itemId") Long itemId, @Param("endTime") LocalDateTime endTime);
} 