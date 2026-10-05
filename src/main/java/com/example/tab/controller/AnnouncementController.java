package com.example.tab.controller;

import com.example.tab.model.dto.AnnouncementAddDTO;
import com.example.tab.model.dto.AnnouncementUpdateDTO;
import com.example.tab.model.vo.AnnouncementVO;
import com.example.tab.service.AnnouncementService;
import com.example.tab.common.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/announcement")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    /**
     * 查询已发布公告列表
     *
     * 学生、教师、超级管理员都可以访问
     */
    @GetMapping("/list")
    public Result<List<AnnouncementVO>> getPublishedList() {

        List<AnnouncementVO> list = announcementService.getPublishedList();

        return Result.success(list);
    }

    /**
     * 查询管理端公告列表
     *
     * 教师：只能看到自己创建的公告
     * 超级管理员：可以看到所有公告
     */
    @GetMapping("/manage/list")
    public Result<List<AnnouncementVO>> getManageList() {

        List<AnnouncementVO> list = announcementService.getManageList();

        return Result.success(list);
    }

    /**
     * 查询公告详情
     */
    @GetMapping("/{id}")
    public Result<AnnouncementVO> getAnnouncement(
            @PathVariable("id") Long id) {

        AnnouncementVO announcement = announcementService.getAnnouncement(id);

        return Result.success(announcement);
    }

    /**
     * 新增公告
     *
     * 教师 / 超级管理员
     */
    @PostMapping
    public Result<Long> addAnnouncement(
            @Valid @RequestBody AnnouncementAddDTO dto) {

        Long announcementId = announcementService.addAnnouncement(dto);

        return Result.success(
                "公告创建成功",
                announcementId);
    }

    /**
     * 修改公告
     *
     * 教师只能修改自己的公告
     * 超级管理员可以修改所有公告
     */
    @PutMapping("/{id}")
    public Result<Void> updateAnnouncement(
            @PathVariable("id") Long id,
            @Valid @RequestBody AnnouncementUpdateDTO dto) {

        announcementService.updateAnnouncement(id, dto);

        return Result.success("公告修改成功", null);
    }

    /**
     * 发布公告
     */
    @PostMapping("/{id}/publish")
    public Result<Void> publishAnnouncement(
            @PathVariable("id") Long id) {

        announcementService.publishAnnouncement(id);

        return Result.success("公告发布成功", null);
    }

    /**
     * 撤回公告
     */
    @PostMapping("/{id}/revoke")
    public Result<Void> revokeAnnouncement(
            @PathVariable("id") Long id) {

        announcementService.revokeAnnouncement(id);

        return Result.success("公告撤回成功", null);
    }

    /**
     * 删除公告
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteAnnouncement(
            @PathVariable("id") Long id) {

        announcementService.deleteAnnouncement(id);

        return Result.success("公告删除成功", null);
    }

    /**
     * 上传公告图片
     */
    @PostMapping(value = "/{id}/image", consumes = "multipart/form-data")
    public Result<String> uploadAnnouncementImage(
            @PathVariable("id") Long id,
            @RequestParam("file") MultipartFile file) {

        String imageUrl = announcementService.uploadAnnouncementImage(
                id,
                file);

        return Result.success(
                "图片上传成功",
                imageUrl);
    }

    /**
     * 删除公告图片
     */
    @DeleteMapping("/image/{imageId}")
    public Result<Void> deleteAnnouncementImage(
            @PathVariable("imageId") Long imageId) {

        announcementService.deleteAnnouncementImage(
                imageId);

        return Result.success(
                "图片删除成功",
                null);
    }
}