package com.example.tab.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("category")
public class Category {
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属用户ID（0代表公共分类）
     */
    private Long userId;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 分类图标（如 Element Plus 图标名或 SVG）
     */
    private String icon;

    /**
     * 排序权重（越小越靠前）
     */
    private Integer sortOrder;

    /**
     * 是否公开: 1公开, 0私有
     */
    private Integer isPublic;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
