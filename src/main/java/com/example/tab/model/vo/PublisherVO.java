package com.example.tab.model.vo;

import lombok.Data;

@Data
public class PublisherVO {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 用户角色
     */
    private String role;
}