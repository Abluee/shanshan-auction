package com.shanshan.auction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shanshan.auction.model.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
} 