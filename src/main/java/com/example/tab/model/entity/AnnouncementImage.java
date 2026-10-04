
package com.example.tab.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公告图片实体类
 */
@Data
@TableName("sys_announcement_image")
public class AnnouncementImage {

    /**
     * 图片ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属公告ID
     */
    private Long announcementId;

    /**
     * 图片存储路径
     */
    private String imageUrl;

    /**
     * 图片显示顺序
     */
    private Integer sortOrder;

    /**
     * 上传时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}