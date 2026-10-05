package com.example.tab.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.tab.model.entity.Resource;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ResourceMapper extends BaseMapper<Resource> {
}