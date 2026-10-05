package com.example.tab.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SiteUpdateDTO {

    /**
     * 要修改的网址ID
     */
    @Positive(message = "网址ID必须为正数")
    private Long id;

    /**
     * 网址标题
     * 不传则不修改
     */
    @Size(max = 100, message = "网址标题不能超过100个字符")
    private String title;

    /**
     * 网址URL
     * 不传则不修改
     */
    @Size(max = 255, message = "网址URL不能超过255个字符")
    private String url;

    /**
     * 图标地址
     * 不传则不修改
     */
    @Size(max = 255, message = "图标地址不能超过255个字符")
    private String icon;

    /**
     * 网址描述
     * 不传则不修改
     */
    @Size(max = 500, message = "网址描述不能超过500个字符")
    private String description;

    /**
     * 分类ID
     * 不传则不修改
     */
    @Positive(message = "分类ID必须为正数")
    private Long categoryId;

    /**
     * 排序值
     * 不传则不修改
     */
    @Min(value = 0, message = "排序值不能小于0")
    private Integer sort;
}