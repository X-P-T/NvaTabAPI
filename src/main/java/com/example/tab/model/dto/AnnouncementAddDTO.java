package com.example.tab.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AnnouncementAddDTO {

    /**
     * 公告标题
     */
    @NotBlank(message = "公告标题不能为空")
    @Size(max = 150, message = "公告标题长度不能超过150个字符")
    private String title;

    /**
     * 富文本正文
     */
    @NotBlank(message = "公告内容不能为空")
    private String content;

    /**
     * 是否置顶
     * 0：否
     * 1：是
     */
    private Integer isTop;
}