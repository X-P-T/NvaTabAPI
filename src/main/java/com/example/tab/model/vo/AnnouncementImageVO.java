package com.example.tab.model.vo;

import lombok.Data;

@Data
public class AnnouncementImageVO {

    /**
     * 图片ID
     */
    private Long id;

    /**
     * 图片地址
     */
    private String imageUrl;

    /**
     * 图片排序
     */
    private Integer sortOrder;
}