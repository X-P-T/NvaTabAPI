package com.example.tab.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiteMetadataDTO {
    /**
     * 网站标题
     */
    private String title;

    /**
     * 网站描述/简介
     */
    private String description;

    /**
     * 图标 Favicon 地址 (绝对路径)
     */
    private String iconUrl;

    /**
     * 原请求 URL
     */
    private String url;
}