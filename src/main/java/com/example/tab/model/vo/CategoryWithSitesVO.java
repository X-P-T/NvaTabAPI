package com.example.tab.model.vo;

import com.example.tab.model.entity.Category;
import com.example.tab.model.entity.Site;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class CategoryWithSitesVO extends Category {

    /**
     * 该分类下的网址列表
     */
    private List<Site> sites;
}