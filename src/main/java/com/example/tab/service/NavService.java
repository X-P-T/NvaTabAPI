package com.example.tab.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.tab.mapper.CategoryMapper;
import com.example.tab.mapper.SiteMapper;
import com.example.tab.model.entity.Category;
import com.example.tab.model.entity.Site;
import com.example.tab.model.vo.CategoryWithSitesVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NavService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private SiteMapper siteMapper;

    /**
     * 新增分类（自动绑定当前登录的用户）
     */
    public boolean addCategory(Category category) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        category.setUserId(currentUserId);

        if (category.getSortOrder() == null) category.setSortOrder(0);
        if (category.getIsPublic() == null) category.setIsPublic(1); // 默认公开
        if (category.getCreateTime() == null) category.setCreateTime(LocalDateTime.now());

        return categoryMapper.insert(category) > 0;
    }

    /**
     * 删除分类（防越权校验）
     */
    public boolean deleteCategory(Long categoryId) {
        Category category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new RuntimeException("分类不存在");
        }

        Long currentUserId = StpUtil.getLoginIdAsLong();
        if (!category.getUserId().equals(currentUserId)) {
            throw new RuntimeException("无权删除他人的分类");
        }

        return categoryMapper.deleteById(categoryId) > 0;
    }

    /**
     * 新增网址（自动绑定当前登录的用户）
     */
    public boolean addSite(Site site) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        site.setUserId(currentUserId);

        if (site.getSortOrder() == null) site.setSortOrder(0);
        if (site.getClickCount() == null) site.setClickCount(0);
        if (site.getStatus() == null) site.setStatus(1);
        if (site.getCreateTime() == null) site.setCreateTime(LocalDateTime.now());

        return siteMapper.insert(site) > 0;
    }

    /**
     * 删除网址（防越权校验）
     */
    public boolean deleteSite(Long siteId) {
        Site site = siteMapper.selectById(siteId);
        if (site == null) {
            throw new RuntimeException("网址不存在");
        }

        Long currentUserId = StpUtil.getLoginIdAsLong();
        if (!site.getUserId().equals(currentUserId)) {
            throw new RuntimeException("无权删除他人的网址");
        }

        return siteMapper.deleteById(siteId) > 0;
    }

    /**
     * 获取全量或指定创作者的导航树结构
     * @param targetUserId 可选参数，传值时只查指定用户的书签；不传则查全站公开书签
     */
    public List<CategoryWithSitesVO> getNavTree(Long targetUserId) {
        // 1. 查询公开的分类
        LambdaQueryWrapper<Category> categoryQuery = new LambdaQueryWrapper<>();
        categoryQuery.eq(Category::getIsPublic, 1)
                     .orderByAsc(Category::getSortOrder);

        if (targetUserId != null) {
            categoryQuery.eq(Category::getUserId, targetUserId);
        }

        List<Category> categories = categoryMapper.selectList(categoryQuery);
        if (categories == null || categories.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 查询正常可用的网址
        LambdaQueryWrapper<Site> siteQuery = new LambdaQueryWrapper<>();
        siteQuery.eq(Site::getStatus, 1)
                 .orderByAsc(Site::getSortOrder);

        if (targetUserId != null) {
            siteQuery.eq(Site::getUserId, targetUserId);
        }

        List<Site> sites = siteMapper.selectList(siteQuery);

        // 3. 按 categoryId 进行分组映射
        Map<Long, List<Site>> siteMap = sites.stream()
                .filter(s -> s.getCategoryId() != null)
                .collect(Collectors.groupingBy(Site::getCategoryId));

        // 4. 拼装为 VO 嵌套结构
        return categories.stream().map(category -> {
            CategoryWithSitesVO vo = new CategoryWithSitesVO();
            BeanUtils.copyProperties(category, vo);
            vo.setSites(siteMap.getOrDefault(category.getId(), new ArrayList<>()));
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 网址点击量自增 (+1，所有人可操作)
     */
    public void incrementClick(Long siteId) {
        Site site = siteMapper.selectById(siteId);
        if (site != null) {
            site.setClickCount((site.getClickCount() == null ? 0 : site.getClickCount()) + 1);
            siteMapper.updateById(site);
        }
    }
}