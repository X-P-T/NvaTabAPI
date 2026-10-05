package com.example.tab.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 网站元数据 DTO
 *
 * 用于网站元数据解析接口返回解析结果。
 */
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
     * 网站描述
     */
    private String description;

    /**
     * 网站 Favicon 图标地址
     */
    private String iconUrl;

    /**
     * 原始请求 URL
     */
    private String url;
}