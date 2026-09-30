package com.example.tab.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.tab.mapper.CategoryMapper;
import com.example.tab.mapper.SiteMapper;
import com.example.tab.model.entity.Category;
import com.example.tab.model.entity.Site;
import com.example.tab.model.vo.CategoryWithSitesVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NavService {

    private final CategoryMapper categoryMapper;
    private final SiteMapper siteMapper;

    public NavService(CategoryMapper categoryMapper, SiteMapper siteMapper) {
        this.categoryMapper = categoryMapper;
        this.siteMapper = siteMapper;
    }

    /**
     * 获取全量导航树结构（分类+属于该分类的网址列表）
     */
    public List<CategoryWithSitesVO> getNavTree() {
        // 1. 查询所有公开分类，按 sortOrder 升序
        LambdaQueryWrapper<Category> categoryQuery = new LambdaQueryWrapper<>();
        categoryQuery.eq(Category::getIsPublic, 1)
            .orderByAsc(Category::getSortOrder);
        List<Category> categories = categoryMapper.selectList(categoryQuery);

        if (categories.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 查询所有正常状态的网址，按 sortOrder 升序
        LambdaQueryWrapper<Site> siteQuery = new LambdaQueryWrapper<>();
        siteQuery.eq(Site::getStatus, 1)
                .orderByAsc(Site::getSortOrder);
        List<Site> sites = siteMapper.selectList(siteQuery);

        // 3. 将网址按 categoryId 进行分组 (Java 8 Stream)
        Map<Long, List<Site>> siteMap = sites.stream()
                .collect(Collectors.groupingBy(site -> site.getCategoryId()));

        // 4. 组装成 VO 返回
        return categories.stream().map(category -> {
            CategoryWithSitesVO vo = new CategoryWithSitesVO();
            BeanUtils.copyProperties(category, vo);
            vo.setSites(siteMap.getOrDefault(category.getId(), new ArrayList<>()));
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 新增网址
     */
    public boolean addSite(Site site) {
        if (site.getSortOrder() == null) {
            site.setSortOrder(0);
        }
        if (site.getClickCount() == null) {
            site.setClickCount(0);
        }
        if (site.getStatus() == null) {
            site.setStatus(1);
        }
        return siteMapper.insert(site) > 0;
    }

    /**
     * 点击量自增 (+1)
     */
    public void incrementClick(Long siteId) {
        Site site = siteMapper.selectById(siteId);
        if (site != null) {
            site.setClickCount((site.getClickCount() == null ? 0 : site.getClickCount()) + 1);
            siteMapper.updateById(site);
        }
    }
}