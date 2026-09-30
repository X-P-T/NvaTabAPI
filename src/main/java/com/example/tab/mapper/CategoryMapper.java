package com.example.tab.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.tab.model.entity.Category;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
