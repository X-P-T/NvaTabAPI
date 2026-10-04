package com.example.tab.controller;

//import com.example.tab.model.entity.Category;
//import com.example.tab.model.entity.Site;
import com.example.tab.common.Result;
import com.example.tab.util.PermissionUtils;
import com.example.tab.model.dto.CategoryDTO;
import com.example.tab.model.dto.SiteDTO;
import com.example.tab.model.vo.CategoryVO;
import com.example.tab.model.vo.CategoryWithSitesVO;
import com.example.tab.service.NavService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

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
    public Result<CategoryVO> addCategory(@Valid @RequestBody CategoryDTO dto) {
        CategoryVO categoryVO = navService.addCategory(dto);
        return Result.success("新增分类成功", categoryVO);
    }

    // 修改分类
    @PutMapping("/category")
    public Result<Void> updateCategory(@Valid @RequestBody CategoryDTO dto) {
        navService.updateCategory(dto);
        return Result.success("修改分类成功", null);
    }

    // 删除分类
    @DeleteMapping("/category/{id}")
    public Result<Void> deleteCategory(@PathVariable("id") Long id) {
        navService.deleteCategory(id);
        return Result.success("删除分类成功", null);
    }

    // 新增网址
    @PostMapping("/site")
    public Result<Void> addSite(@Valid @RequestBody SiteDTO dto) {
        navService.addSite(dto);
        return Result.success("新增网址成功", null);
    }

    // 修改网址
    @PutMapping("/site")
    public Result<Void> updateSite(@Valid @RequestBody SiteDTO dto) {
        navService.updateSite(dto);
        return Result.success("修改网址成功", null);
    }

    // 删除网址
    @DeleteMapping("/site/{id}")
    public Result<Void> deleteSite(@PathVariable("id") Long id) {
        navService.deleteSite(id);
        return Result.success("删除网址成功", null);
    }

    /**
     * 记录网址点击量 (公开接口，所有人可触发)
     */
    @PostMapping("/site/click/{id}")
    public Result<Void> incrementClick(@PathVariable Long id) {
        navService.incrementClick(id);
        return Result.success("计数成功", null);
    }
}