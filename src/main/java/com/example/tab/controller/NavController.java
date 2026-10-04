package com.example.tab.controller;

import com.example.tab.model.entity.Category;
import com.example.tab.model.entity.Site;
import com.example.tab.model.dto.CategoryDTO;
import com.example.tab.model.dto.SiteDTO;
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

    // 新增分类
    @PostMapping("/category")
    public String addCategory(@RequestBody CategoryDTO dto) {
        navService.addCategory(dto);
        return "新增分类成功";
    }

    // 修改分类
    @PutMapping("/category")
    public String updateCategory(@RequestBody CategoryDTO dto) {
        navService.updateCategory(dto);
        return "修改分类成功";
    }

    // 删除分类
    @DeleteMapping("/category/{id}")
    public String deleteCategory(@PathVariable("id") Long id) {
        navService.deleteCategory(id);
        return "删除分类成功";
    }

    // 新增网址
    @PostMapping("/site")
    public String addSite(@RequestBody SiteDTO dto) {
        navService.addSite(dto);
        return "新增网址成功";
    }

    // 修改网址
    @PutMapping("/site")
    public String updateSite(@RequestBody SiteDTO dto) {
        navService.updateSite(dto);
        return "修改网址成功";
    }

    // 删除网址
    @DeleteMapping("/site/{id}")
    public String deleteSite(@PathVariable("id") Long id) {
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