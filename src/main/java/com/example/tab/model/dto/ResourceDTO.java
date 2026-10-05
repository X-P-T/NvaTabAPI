package com.example.tab.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResourceDTO {

    /**
     * 资源标题
     */
    @NotBlank(message = "资源标题不能为空")
    @Size(max = 200, message = "资源标题不能超过200个字符")
    private String title;

    /**
     * 资源描述
     */
    @Size(max = 2000, message = "资源描述不能超过2000个字符")
    private String description;

    /**
     * 资源链接
     */
    @NotBlank(message = "资源链接不能为空")
    @Size(max = 1000, message = "资源链接不能超过1000个字符")
    private String url;
}