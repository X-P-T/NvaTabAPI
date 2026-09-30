package com.example.tab.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("site")
public class Site {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属分类ID
     */
    private Long categoryId;

    /**
     * 所属用户ID（0代表公共网址）
     */
    private Long userId;

    /**
     * 网站标题
     */
    private String title;

    /**
     * 网站链接
     */
    private String url;

    /**
     * 图标地址
     */
    private String iconUrl;

    /**
     * 网站描述
     */
    private String description;

    /**
     * 点击量
     */
    private Integer clickCount;

    /**
     * 排序权重（越小越靠前）
     */
    private Integer sortOrder;

    /**
     * 状态: 1正常, 0失效
     */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}