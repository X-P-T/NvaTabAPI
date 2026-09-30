package com.example.tab.controller;

import com.example.tab.model.dto.SiteMetadataDTO;
import com.example.tab.util.WebMetadataUtil;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tool")
public class ToolController {
    /**
     * 自动解析网站元数据接口
     * 示例: GET /api/tool/parse-site?url=https://github.com
     */
    @GetMapping("/parse-site")
    public SiteMetadataDTO parseSite(@RequestParam("url") String url) {
        return WebMetadataUtil.parseMetadata(url);
    }
}
