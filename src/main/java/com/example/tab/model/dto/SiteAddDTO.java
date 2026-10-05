package com.example.tab.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SiteAddDTO {

    @NotBlank(message = "网址标题不能为空")
    @Size(max = 100, message = "网址标题不能超过100个字符")
    private String title;

    @NotBlank(message = "网址URL不能为空")
    @Size(max = 255, message = "网址URL不能超过255个字符")
    private String url;

    @Size(max = 255, message = "图标地址不能超过255个字符")
    private String icon;

    @Size(max = 500, message = "网址描述不能超过500个字符")
    private String description;

    @NotNull(message = "分类ID不能为空")
    @Positive(message = "分类ID必须为正数")
    private Long categoryId;

    @Min(value = 0, message = "排序值不能小于0")
    private Integer sort;
}