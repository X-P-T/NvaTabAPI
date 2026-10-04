package com.example.tab.model.dto;

import lombok.Data;

@Data
public class SiteDTO {
    private Long id;
    private String title;
    private String url;
    private String icon;
    private String description;
    private Long categoryId;
    private Integer sort;
}
