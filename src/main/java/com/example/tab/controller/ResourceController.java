package com.example.tab.controller;

import com.example.tab.model.dto.ResourceDTO;
import com.example.tab.model.entity.Resource;
import com.example.tab.service.ResourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resource")
@RequiredArgsConstructor
public class ResourceController {
    private final ResourceService resourceService;

    /**
     * 获取资源列表
     */
    @GetMapping("/list")
    public List<Resource> list() {
        return resourceService.list();
    }

    /**
     * 获取资源详情
     */
    @GetMapping("/{id}")
    public Resource getById(@PathVariable Long id) {
        return resourceService.getById(id);
    }

    /**
     * 创建资源
     */
    @PostMapping
    public void create(@Valid @RequestBody ResourceDTO dto) {
        resourceService.create(dto);
    }

    /**
     * 修改资源
     */
    @PutMapping("/{id}")
    public void update(
            @PathVariable Long id,
            @Valid @RequestBody ResourceDTO dto) {
        resourceService.update(id, dto);
    }

    /**
     * 删除资源
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        resourceService.delete(id);
    }
}
