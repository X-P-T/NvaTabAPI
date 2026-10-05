package com.example.tab.service;

import cn.dev33.satoken.stp.StpUtil;
import com.example.tab.mapper.ResourceMapper;
import com.example.tab.model.dto.ResourceDTO;
import com.example.tab.model.entity.Resource;
import com.example.tab.util.PermissionUtils;
import com.example.tab.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final ResourceMapper resourceMapper;

    /**
     * 获取资源列表
     */
    public List<Resource> list() {
        return resourceMapper.selectList(null);
    }

    /**
     * 根据ID获取资源
     */
    public Resource getById(Long id) {
        Resource resource = resourceMapper.selectById(id);

        if (resource == null) {
            throw BusinessException.notFound("资源不存在");
        }

        return resource;
    }

    /**
     * 创建资源
     */
    public void create(ResourceDTO dto) {

        // 只有教师和超管可以创建
        PermissionUtils.checkTeacherOrAdmin();

        Resource resource = new Resource();

        BeanUtils.copyProperties(dto, resource);

        // 创建者必须由后端根据当前登录用户获取
        resource.setCreatorId(StpUtil.getLoginIdAsLong());

        resourceMapper.insert(resource);
    }

    /**
     * 修改资源
     */
    public void update(Long id, ResourceDTO dto) {

        PermissionUtils.checkTeacherOrAdmin();

        Resource resource = resourceMapper.selectById(id);

        if (resource == null) {
            throw BusinessException.notFound("资源不存在");
        }

        // 超管可以修改任何资源
        // 教师只能修改自己创建的资源
        if (!PermissionUtils.isSuperAdmin()
                && !resource.getCreatorId().equals(StpUtil.getLoginIdAsLong())) {
            throw BusinessException.forbidden("无权修改其他教师的资源");
        }

        BeanUtils.copyProperties(dto, resource);

        resourceMapper.updateById(resource);
    }

    /**
     * 删除资源
     */
    public void delete(Long id) {

        PermissionUtils.checkTeacherOrAdmin();

        Resource resource = resourceMapper.selectById(id);

        if (resource == null) {
            throw BusinessException.notFound("资源不存在");
        }

        // 超管可以删除任何资源
        // 教师只能删除自己创建的资源
        if (!PermissionUtils.isSuperAdmin()
                && !resource.getCreatorId().equals(StpUtil.getLoginIdAsLong())) {
            throw BusinessException.forbidden("无权删除其他教师的资源");
        }

        resourceMapper.deleteById(id);
    }
}