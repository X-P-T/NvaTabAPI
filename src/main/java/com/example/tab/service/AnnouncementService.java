package com.example.tab.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.tab.exception.BusinessException;
import com.example.tab.mapper.AnnouncementImageMapper;
import com.example.tab.mapper.AnnouncementMapper;
import com.example.tab.mapper.UserMapper;
import com.example.tab.model.dto.AnnouncementAddDTO;
import com.example.tab.model.dto.AnnouncementUpdateDTO;
import com.example.tab.model.entity.Announcement;
import com.example.tab.model.entity.AnnouncementImage;
import com.example.tab.model.entity.User;
import com.example.tab.model.vo.AnnouncementImageVO;
import com.example.tab.model.vo.AnnouncementVO;
import com.example.tab.model.vo.PublisherVO;
import com.example.tab.util.PermissionUtils;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import cn.dev33.satoken.stp.StpUtil;

import java.time.LocalDateTime;
// import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementMapper announcementMapper;
    private final AnnouncementImageMapper announcementImageMapper;
    private final UserMapper userMapper;

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    /**
     * 新增公告
     */
    @Transactional
    public Long addAnnouncement(AnnouncementAddDTO dto) {

        PermissionUtils.checkTeacherOrAdmin();

        Long userId = StpUtil.getLoginIdAsLong();

        String title = dto.getTitle().trim();
        String content = sanitizeHtml(dto.getContent());

        if (title.isEmpty()) {
            throw BusinessException.badRequest("公告标题不能为空");
        }

        if (content.isBlank()) {
            throw BusinessException.badRequest("公告内容不能为空");
        }

        Announcement announcement = new Announcement();

        announcement.setUserId(userId);
        announcement.setTitle(title);
        announcement.setContent(content);
        announcement.setStatus(0);

        Integer isTop = dto.getIsTop();

        if (isTop == null) {
            isTop = 0;
        }

        if (isTop != 0 && isTop != 1) {
            throw BusinessException.badRequest("置顶状态参数错误");
        }

        announcement.setIsTop(isTop);
        announcement.setPublishTime(null);

        announcementMapper.insert(announcement);

        return announcement.getId();
    }

    /**
     * 修改公告
     */
    @Transactional
    public void updateAnnouncement(
            Long announcementId,
            AnnouncementUpdateDTO dto) {

        if (announcementId == null) {
            throw BusinessException.badRequest("公告ID不能为空");
        }

        Announcement announcement = announcementMapper.selectById(announcementId);

        if (announcement == null) {
            throw BusinessException.notFound("公告不存在");
        }

        checkManagePermission(announcement);

        boolean changed = false;

        if (dto.getTitle() != null) {

            String title = dto.getTitle().trim();

            if (title.isEmpty()) {
                throw BusinessException.badRequest("公告标题不能为空");
            }

            announcement.setTitle(title);

            changed = true;
        }

        if (dto.getContent() != null) {

            String content = sanitizeHtml(dto.getContent());

            if (content.isBlank()) {
                throw BusinessException.badRequest("公告内容不能为空");
            }

            announcement.setContent(content);

            changed = true;
        }

        if (dto.getIsTop() != null) {

            Integer isTop = dto.getIsTop();

            if (isTop != 0 && isTop != 1) {
                throw BusinessException.badRequest("置顶状态参数错误");
            }

            announcement.setIsTop(isTop);

            changed = true;
        }

        if (!changed) {
            throw BusinessException.badRequest("没有需要修改的公告信息");
        }

        announcementMapper.updateById(announcement);
    }

    /**
     * 查询公告详情
     */
    public AnnouncementVO getAnnouncement(Long announcementId) {

        if (announcementId == null) {
            throw BusinessException.badRequest("公告ID不能为空");
        }

        Announcement announcement = announcementMapper.selectById(announcementId);

        if (announcement == null) {
            throw BusinessException.notFound("公告不存在");
        }

        /*
         * 已发布公告：
         * 所有登录用户都可以查看
         */
        if (announcement.getStatus() == 1) {
            return buildAnnouncementVO(announcement);
        }

        /*
         * 草稿 / 已撤回：
         * 只有作者本人或超级管理员可以查看
         */
        checkManagePermission(announcement);

        return buildAnnouncementVO(announcement);
    }

    /**
     * 查询已发布公告列表
     */
    public List<AnnouncementVO> getPublishedList() {

        LambdaQueryWrapper<Announcement> query = new LambdaQueryWrapper<>();

        query.eq(Announcement::getStatus, 1)
                .orderByDesc(Announcement::getIsTop)
                .orderByDesc(Announcement::getPublishTime)
                .orderByDesc(Announcement::getId);

        List<Announcement> announcements = announcementMapper.selectList(query);

        return announcements.stream()
                .map(this::buildAnnouncementVO)
                .collect(Collectors.toList());
    }

    /**
     * 查询管理端公告列表
     */
    public List<AnnouncementVO> getManageList() {

        PermissionUtils.checkTeacherOrAdmin();

        Long currentUserId = StpUtil.getLoginIdAsLong();

        LambdaQueryWrapper<Announcement> query = new LambdaQueryWrapper<>();

        if (!PermissionUtils.isSuperAdmin()) {
            query.eq(Announcement::getUserId, currentUserId);
        }

        query.orderByDesc(Announcement::getIsTop)
                .orderByDesc(Announcement::getUpdateTime)
                .orderByDesc(Announcement::getId);

        List<Announcement> announcements = announcementMapper.selectList(query);

        return announcements.stream()
                .map(this::buildAnnouncementVO)
                .collect(Collectors.toList());
    }

    /**
     * 发布公告
     */
    @Transactional
    public void publishAnnouncement(Long announcementId) {

        if (announcementId == null) {
            throw BusinessException.badRequest("公告ID不能为空");
        }

        Announcement announcement = announcementMapper.selectById(announcementId);

        if (announcement == null) {
            throw BusinessException.notFound("公告不存在");
        }

        checkManagePermission(announcement);

        if (announcement.getStatus() == 1) {
            throw BusinessException.badRequest("公告已经发布");
        }

        announcement.setStatus(1);
        announcement.setPublishTime(LocalDateTime.now());

        announcementMapper.updateById(announcement);
    }

    /**
     * 撤回公告
     */
    @Transactional
    public void revokeAnnouncement(Long announcementId) {

        if (announcementId == null) {
            throw BusinessException.badRequest("公告ID不能为空");
        }

        Announcement announcement = announcementMapper.selectById(announcementId);

        if (announcement == null) {
            throw BusinessException.notFound("公告不存在");
        }

        checkManagePermission(announcement);

        if (announcement.getStatus() != 1) {
            throw BusinessException.badRequest("只有已发布公告才能撤回");
        }

        announcement.setStatus(2);

        announcementMapper.updateById(announcement);
    }

    /**
     * 删除公告
     */
    @Transactional
    public void deleteAnnouncement(Long announcementId) {

        if (announcementId == null) {
            throw BusinessException.badRequest("公告ID不能为空");
        }

        Announcement announcement = announcementMapper.selectById(announcementId);

        if (announcement == null) {
            throw BusinessException.notFound("公告不存在");
        }

        checkManagePermission(announcement);

        if (announcement.getStatus() == 1) {
            throw BusinessException.badRequest(
                    "已发布公告不能直接删除，请先撤回公告");
        }

        LambdaQueryWrapper<AnnouncementImage> imageQuery = new LambdaQueryWrapper<>();

        imageQuery.eq(
                AnnouncementImage::getAnnouncementId,
                announcementId);

        List<AnnouncementImage> images = announcementImageMapper.selectList(
                imageQuery);

        /*
         * 删除服务器上的图片文件
         */
        for (AnnouncementImage image : images) {
            deleteImageFile(image.getImageUrl());
        }

        /*
         * 删除数据库中的图片记录
         */
        announcementImageMapper.delete(imageQuery);

        /*
         * 删除公告
         */
        announcementMapper.deleteById(announcementId);
    }

    /**
     * 检查公告管理权限
     */
    private void checkManagePermission(Announcement announcement) {

        if (PermissionUtils.isSuperAdmin()) {
            return;
        }

        if (!PermissionUtils.isTeacher()) {
            throw new BusinessException(
                    403,
                    "权限不足，仅教师或超级管理员可以操作公告");
        }

        Long currentUserId = StpUtil.getLoginIdAsLong();

        if (!currentUserId.equals(announcement.getUserId())) {
            throw new BusinessException(
                    403,
                    "无权操作其他教师发布的公告");
        }
    }

    /**
     * 构建公告 VO
     */
    private AnnouncementVO buildAnnouncementVO(
            Announcement announcement) {

        AnnouncementVO vo = new AnnouncementVO();

        vo.setId(announcement.getId());
        vo.setTitle(announcement.getTitle());
        vo.setContent(announcement.getContent());
        vo.setStatus(announcement.getStatus());
        vo.setIsTop(announcement.getIsTop());
        vo.setPublishTime(announcement.getPublishTime());
        vo.setCreateTime(announcement.getCreateTime());
        vo.setUpdateTime(announcement.getUpdateTime());

        // 查询发布人
        User publisher = userMapper.selectById(announcement.getUserId());

        if (publisher != null) {

            PublisherVO publisherVO = new PublisherVO();

            publisherVO.setId(publisher.getId());
            publisherVO.setUsername(publisher.getUsername());
            publisherVO.setNickname(publisher.getNickname());
            publisherVO.setAvatar(publisher.getAvatar());
            publisherVO.setRole(publisher.getRole());

            vo.setPublisher(publisherVO);
        }

        // 查询公告图片
        LambdaQueryWrapper<AnnouncementImage> imageQuery = new LambdaQueryWrapper<>();

        imageQuery.eq(
                AnnouncementImage::getAnnouncementId,
                announcement.getId())
                .orderByAsc(AnnouncementImage::getSortOrder)
                .orderByAsc(AnnouncementImage::getId);

        List<AnnouncementImage> images = announcementImageMapper.selectList(imageQuery);

        List<AnnouncementImageVO> imageVOList = images.stream()
                .map(image -> {

                    AnnouncementImageVO imageVO = new AnnouncementImageVO();

                    imageVO.setId(image.getId());
                    imageVO.setImageUrl(
                            image.getImageUrl());
                    imageVO.setSortOrder(
                            image.getSortOrder());

                    return imageVO;
                })
                .collect(Collectors.toList());

        vo.setImages(imageVOList);

        return vo;
    }

    /**
     * 富文本 HTML 安全过滤
     */
    private String sanitizeHtml(String content) {

        if (content == null) {
            return "";
        }

        Safelist safelist = Safelist.relaxed()
                .addTags(
                        "img",
                        "video",
                        "source")
                .addAttributes(
                        "img",
                        "src",
                        "alt",
                        "width",
                        "height")
                .addAttributes(
                        "a",
                        "target",
                        "rel")
                .addProtocols(
                        "img",
                        "src",
                        "http",
                        "https")
                .addProtocols(
                        "a",
                        "href",
                        "http",
                        "https");

        return Jsoup.clean(content, safelist).trim();
    }

    /**
     * 上传公告图片
     */
    @Transactional
    public String uploadAnnouncementImage(
            Long announcementId,
            MultipartFile file) {

        if (announcementId == null) {
            throw BusinessException.badRequest("公告ID不能为空");
        }

        if (file == null || file.isEmpty()) {
            throw BusinessException.badRequest("请选择要上传的图片");
        }

        Announcement announcement = announcementMapper.selectById(announcementId);

        if (announcement == null) {
            throw BusinessException.notFound("公告不存在");
        }

        /*
         * 只有公告作者或超级管理员
         * 可以给公告上传图片
         */
        checkManagePermission(announcement);

        /*
         * 限制文件大小：5MB
         */
        if (file.getSize() > 5 * 1024 * 1024) {
            throw BusinessException.badRequest(
                    "图片大小不能超过5MB");
        }

        /*
         * 获取原始文件名
         */
        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null
                || originalFilename.isBlank()) {
            throw BusinessException.badRequest(
                    "图片文件名不能为空");
        }

        /*
         * 获取扩展名
         */
        String extension = "";

        int dotIndex = originalFilename.lastIndexOf(".");

        if (dotIndex >= 0) {
            extension = originalFilename
                    .substring(dotIndex)
                    .toLowerCase();
        }

        /*
         * 只允许常见图片格式
         */
        if (!extension.equals(".jpg")
                && !extension.equals(".jpeg")
                && !extension.equals(".png")
                && !extension.equals(".gif")
                && !extension.equals(".webp")) {

            throw BusinessException.badRequest(
                    "只支持 JPG、JPEG、PNG、GIF、WEBP 格式的图片");
        }

        /*
         * 检查 MIME 类型
         */
        String contentType = file.getContentType();

        if (contentType == null
                || !contentType.startsWith("image/")) {

            throw BusinessException.badRequest(
                    "上传的文件不是有效的图片");
        }

        /*
         * 生成随机文件名
         *
         * 不使用用户原始文件名，
         * 防止文件名冲突和路径问题。
         */
        String filename = UUID.randomUUID()
                .toString()
                .replace("-", "")
                + extension;

        /*
         * 创建目录
         */
        Path directory = Paths.get(uploadDir, "announcement")
                .toAbsolutePath()
                .normalize();

        try {

            Files.createDirectories(directory);

            /*
             * 最终保存路径
             */
            Path target = directory.resolve(filename)
                    .normalize();

            /*
             * 防止路径逃逸
             */
            if (!target.startsWith(directory)) {

                throw BusinessException.badRequest(
                        "非法文件路径");
            }

            /*
             * 保存文件
             */
            file.transferTo(target);

            /*
             * 保存图片记录
             */
            AnnouncementImage image = new AnnouncementImage();

            image.setAnnouncementId(
                    announcementId);

            image.setImageUrl(
                    "/uploads/announcement/" + filename);

            image.setSortOrder(0);

            announcementImageMapper.insert(image);

            /*
             * 返回给前端的 URL
             */
            return image.getImageUrl();

        } catch (IOException e) {

            throw new BusinessException(
                    500,
                    "图片上传失败，请稍后重试");
        }
    }

    /**
     * 删除公告图片
     */
    @Transactional
    public void deleteAnnouncementImage(Long imageId) {

        if (imageId == null) {
            throw BusinessException.badRequest("图片ID不能为空");
        }

        AnnouncementImage image = announcementImageMapper.selectById(imageId);

        if (image == null) {
            throw BusinessException.notFound("公告图片不存在");
        }

        Announcement announcement = announcementMapper.selectById(
                image.getAnnouncementId());

        if (announcement == null) {
            throw BusinessException.notFound("关联公告不存在");
        }

        checkManagePermission(announcement);

        deleteImageFile(image.getImageUrl());

        announcementImageMapper.deleteById(imageId);
    }

    /**
     * 删除服务器上的图片文件
     */
    private void deleteImageFile(String imageUrl) {

        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        String prefix = "/uploads/";

        if (!imageUrl.startsWith(prefix)) {
            throw BusinessException.badRequest(
                    "非法图片路径");
        }

        String relativePath = imageUrl.substring(prefix.length());

        Path uploadRoot = Paths.get(uploadDir)
                .toAbsolutePath()
                .normalize();

        Path target = uploadRoot
                .resolve(relativePath)
                .normalize();

        if (!target.startsWith(uploadRoot)) {
            throw BusinessException.badRequest(
                    "非法图片路径");
        }

        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            throw new BusinessException(
                    500,
                    "图片文件删除失败");
        }
    }
}