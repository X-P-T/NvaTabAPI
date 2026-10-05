package com.example.tab.model.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AnnouncementUpdateDTO {

    /**
     * 公告标题
     */
    @Size(max = 150, message = "公告标题长度不能超过150个字符")
    private String title;

    /**
     * 富文本正文
     */
    private String content;

    /**
     * 是否置顶
     * 0：否
     * 1：是
     */
    private Integer isTop;
}