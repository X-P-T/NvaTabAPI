
package com.example.tab.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公告实体类
 */
@Data
@TableName("sys_announcement")
public class Announcement {

    /**
     * 公告ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 发布者用户ID
     */
    private Long userId;

    /**
     * 公告标题
     */
    private String title;

    /**
     * 公告正文，支持富文本
     */
    private String content;

    /**
     * 公告状态：0草稿，1已发布，2已撤回
     */
    private Integer status;

    /**
     * 是否置顶：0否，1是
     */
    private Integer isTop;

    /**
     * 发布时间
     */
    private LocalDateTime publishTime;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}