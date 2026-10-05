package com.example.tab.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.tab.model.entity.Announcement;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AnnouncementMapper extends BaseMapper<Announcement> {
}