package com.example.tab.controller;

import com.example.tab.model.dto.SiteMetadataDTO;
import com.example.tab.util.WebMetadataUtil;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
// import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 自动解析网站元数据接口
 * ToolController
 */
@RestController
@RequestMapping("/api/tool")
@Validated
@RequiredArgsConstructor
public class ToolController {

    @GetMapping("/parse-site")
    public SiteMetadataDTO parseSite(
            @RequestParam("url") @NotBlank(message = "网址不能为空") String url) {

        return WebMetadataUtil.parseMetadata(url.trim());
    }

}
