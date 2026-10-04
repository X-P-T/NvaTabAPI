package com.example.tab.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.tab.model.entity.Site;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SiteMapper extends BaseMapper<Site> {
    @Update("""
            UPDATE site
            SET click_count = COALESCE(click_count, 0) + 1
            WHERE id = #{siteId}
              AND status = 1
            """)
    int incrementClick(@Param("siteId") Long siteId);
}