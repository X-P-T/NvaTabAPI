package com.example.tab.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.tab.exception.BusinessException;
import com.example.tab.mapper.CategoryMapper;
import com.example.tab.mapper.SiteMapper;
import com.example.tab.model.dto.CategoryDTO;
import com.example.tab.model.dto.SiteDTO;
import com.example.tab.model.entity.Category;
import com.example.tab.model.entity.Site;
import com.example.tab.model.vo.CategoryWithSitesVO;
import com.example.tab.util.PermissionUtils;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    // ------------------- 分类管理 -------------------

    /**
     * 新增分类
     * 仅教师和超级管理员允许
     */
    public void addCategory(CategoryDTO dto) {
        PermissionUtils.checkTeacherOrAdmin();

        Long currentUserId = StpUtil.getLoginIdAsLong();

        Category category = new Category();
        category.setName(dto.getName());
        category.setSortOrder(dto.getSort() != null ? dto.getSort() : 0);
        category.setUserId(currentUserId);

        categoryMapper.insert(category);
    }

    // 修改分类
    public void updateCategory(CategoryDTO dto) {
        if (dto.getId() == null) {
            throw new RuntimeException("分类ID不能为空");
        }
        Category category = categoryMapper.selectById(dto.getId());
        if (category == null) {
            throw new RuntimeException("分类不存在");
        }

        // 鉴权：SUPER_ADMIN 权限放行 或 只能修改属于自己的分类
        PermissionUtils.checkOwnerOrAdmin(category.getUserId());

        category.setName(dto.getName());
        if (dto.getSort() != null) {
            category.setSortOrder(dto.getSort());
        }
        categoryMapper.updateById(category);
    }

    /**
     * 删除分类及其下属网址
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long categoryId) {
        Category category = categoryMapper.selectById(categoryId);

        if (category == null) {
            throw new RuntimeException("分类不存在");
        }

        PermissionUtils.checkOwnerOrAdmin(category.getUserId());

        LambdaQueryWrapper<Site> siteQuery = new LambdaQueryWrapper<>();
        siteQuery.eq(Site::getCategoryId, categoryId);

        siteMapper.delete(siteQuery);
        categoryMapper.deleteById(categoryId);
    }

    // ------------------- 网址书签管理 -------------------

    /**
     * 新增网址
     * 仅教师和超级管理员允许
     */
    public void addSite(SiteDTO dto) {
        PermissionUtils.checkTeacherOrAdmin();

        Long currentUserId = StpUtil.getLoginIdAsLong();

        // 校验分类是否存在且属于当前用户
        Category category = categoryMapper.selectById(dto.getCategoryId());

        if (category == null) {
            throw new RuntimeException("分类不存在");
        }

        if (!PermissionUtils.isSuperAdmin()
                && !currentUserId.equals(category.getUserId())) {
            throw new RuntimeException("不能在他人的分类下创建网址");
        }

        Site site = new Site();
        site.setTitle(dto.getTitle());
        site.setUrl(dto.getUrl());
        site.setIconUrl(dto.getIcon());
        site.setDescription(dto.getDescription());
        site.setCategoryId(dto.getCategoryId());
        site.setSortOrder(dto.getSort() != null ? dto.getSort() : 0);
        site.setClickCount(0);
        site.setUserId(currentUserId);

        siteMapper.insert(site);
    }

    // 修改网址
    public void updateSite(SiteDTO dto) {
        if (dto.getId() == null) {
            throw new RuntimeException("网址ID不能为空");
        }
        Site site = siteMapper.selectById(dto.getId());
        if (site == null) {
            throw BusinessException.notFound("网址不存在");
        }

        // 校验目标分类
        Category category = categoryMapper.selectById(dto.getCategoryId());

        if (category == null) {
            throw new RuntimeException("目标分类不存在");
        }

        Long currentUserId = StpUtil.getLoginIdAsLong();
        if (!PermissionUtils.isSuperAdmin()
                && !currentUserId.equals(category.getUserId())) {
            throw new RuntimeException("不能将网址移动到他人的分类");
        }

        // 鉴权：SUPER_ADMIN 权限放行 或 只能修改属于自己的网址
        PermissionUtils.checkOwnerOrAdmin(site.getUserId());

        site.setTitle(dto.getTitle());
        site.setUrl(dto.getUrl());
        site.setIconUrl(dto.getIcon());
        site.setDescription(dto.getDescription());
        site.setCategoryId(dto.getCategoryId());
        if (dto.getSort() != null) {
            site.setSortOrder(dto.getSort());
        }
        siteMapper.updateById(site);
    }

    // 删除网址
    public void deleteSite(Long siteId) {
        Site site = siteMapper.selectById(siteId);
        if (site == null) {
            throw new RuntimeException("网址不存在");
        }

        // 鉴权：SUPER_ADMIN 权限放行 或 只能删除属于自己的网址
        PermissionUtils.checkOwnerOrAdmin(site.getUserId());

        siteMapper.deleteById(siteId);
    }

    /**
     * 获取全量或指定创作者的导航树结构
     * 
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