package com.example.tab.controller;

import com.example.tab.model.entity.Category;
import com.example.tab.model.entity.Site;
import com.example.tab.model.vo.CategoryWithSitesVO;
import com.example.tab.service.NavService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/nav")
public class NavController {

    @Autowired
    private NavService navService;

    /**
     * 查询导航树 (公开接口，所有人均可访问)
     * GET /api/nav/tree
     * GET /api/nav/tree?userId=1 (查指定作者)
     */
    @GetMapping("/tree")
    public List<CategoryWithSitesVO> getNavTree(@RequestParam(required = false) Long userId) {
        return navService.getNavTree(userId);
    }

    /**
     * 新增分类 (需携带 Token 认证)
     */
    @PostMapping("/category")
    public String addCategory(@RequestBody Category category) {
        navService.addCategory(category);
        return "新增分类成功";
    }

    /**
     * 删除分类 (需携带 Token 认证且必须是自己的数据)
     */
    @DeleteMapping("/category/{id}")
    public String deleteCategory(@PathVariable Long id) {
        navService.deleteCategory(id);
        return "删除分类成功";
    }

    /**
     * 新增网址 (需携带 Token 认证)
     */
    @PostMapping("/site")
    public String addSite(@RequestBody Site site) {
        navService.addSite(site);
        return "新增网址成功";
    }

    /**
     * 删除网址 (需携带 Token 认证且必须是自己的数据)
     */
    @DeleteMapping("/site/{id}")
    public String deleteSite(@PathVariable Long id) {
        navService.deleteSite(id);
        return "删除网址成功";
    }

    /**
     * 记录网址点击量 (公开接口，所有人可触发)
     */
    @PostMapping("/site/click/{id}")
    public String incrementClick(@PathVariable Long id) {
        navService.incrementClick(id);
        return "计数成功";
    }
}