package com.example.tab.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.tab.model.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}