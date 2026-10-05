package com.example.tab.model.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AnnouncementVO {

    /**
     * 公告ID
     */
    private Long id;

    /**
     * 公告标题
     */
    private String title;

    /**
     * 富文本正文
     */
    private String content;

    /**
     * 公告状态
     * 0：草稿
     * 1：已发布
     * 2：已撤回
     */
    private Integer status;

    /**
     * 是否置顶
     * 0：否
     * 1：是
     */
    private Integer isTop;

    /**
     * 发布时间
     */
    private LocalDateTime publishTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 发布人信息
     */
    private PublisherVO publisher;

    /**
     * 公告图片
     */
    private List<AnnouncementImageVO> images;
}