package com.example.tab.controller;

import com.example.tab.model.entity.Category;
import com.example.tab.model.entity.Site;
import com.example.tab.model.vo.CategoryWithSitesVO;
import com.example.tab.service.NavService;
import com.example.tab.mapper.CategoryMapper;
// import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/nav")
public class NavController {

    private final NavService navService;

    private final CategoryMapper categoryMapper;

    public NavController(NavService navService, CategoryMapper categoryMapper) {
        this.navService = navService;
        this.categoryMapper = categoryMapper;
    }

    /**
     * 获取完整导航树数据（首页渲染专用）
     * GET /api/nav/tree
     */
    @GetMapping("/tree")
    public List<CategoryWithSitesVO> getNavTree() {
        return navService.getNavTree();
    }

    /**
     * 新增分类
     * POST /api/nav/category
     */
    @PostMapping("/category")
    public String addCategory(@RequestBody Category category) {
        categoryMapper.insert(category);
        return "新增分类成功";
    }

    /**
     * 新增网址卡片
     * POST /api/nav/site
     */
    @PostMapping("/site")
    public String addSite(@RequestBody Site site) {
        navService.addSite(site);
        return "新增网址成功";
    }

    /**
     * 累加网址点击量
     * POST /api/nav/site/click/{id}
     */
    @PostMapping("/site/click/{id}")
    public String clickSite(@PathVariable("id") Long id) {
        navService.incrementClick(id);
        return "OK";
    }
}