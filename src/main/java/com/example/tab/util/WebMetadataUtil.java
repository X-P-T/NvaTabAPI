package com.example.tab.util;

import com.example.tab.model.dto.SiteMetadataDTO;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.util.StringUtils;

import java.net.URI;
// import java.net.URL;

@Slf4j
public class WebMetadataUtil {

    // 伪装常见浏览器 User-Agent，防止部分网站屏蔽爬虫 (403)
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    // 超时时间 5 秒
    private static final int TIMEOUT_MS = 5000;

    /**
     * 抓取指定 URL 的网页元信息
     */
    public static SiteMetadataDTO parseMetadata(String targetUrl) {
        if (!StringUtils.hasText(targetUrl)) {
            return new SiteMetadataDTO();
        }

        // 规范化 URL 前缀
        if (!targetUrl.startsWith("http://") && !targetUrl.startsWith("https://")) {
            targetUrl = "https://" + targetUrl;
        }

        try {
            // 1. 发起 HTTP GET 请求获取 HTML 文档
            Document doc = Jsoup.connect(targetUrl)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT_MS)
                    .followRedirects(true) // 自动跟随重定向
                    .get();

            // 2. 解析 Title
            String title = parseTitle(doc);

            // 3. 解析 Description
            String description = parseDescription(doc);

            // 4. 解析 Favicon 图标地址
            String iconUrl = parseFavicon(doc, targetUrl);

            return SiteMetadataDTO.builder()
                    .title(title)
                    .description(description)
                    .iconUrl(iconUrl)
                    .url(targetUrl)
                    .build();

        } catch (Exception e) {
            log.warn("抓取网页元数据失败 [{}]: {}", targetUrl, e.getMessage());
            // 抓取失败时返回基本默认对象，不让程序 crash
            return SiteMetadataDTO.builder()
                    .title(getDomainName(targetUrl))
                    .description("暂无描述")
                    .iconUrl(getFallbackFavicon(targetUrl))
                    .url(targetUrl)
                    .build();
        }
    }

    /**
     * 解析标题
     */
    private static String parseTitle(Document doc) {
        String title = doc.title();
        if (StringUtils.hasText(title)) {
            return title.trim();
        }
        Element ogTitle = doc.selectFirst("meta[property=og:title]");
        if (ogTitle != null && StringUtils.hasText(ogTitle.attr("content"))) {
            return ogTitle.attr("content").trim();
        }
        return "";
    }

    /**
     * 解析描述
     */
    private static String parseDescription(Document doc) {
        Element metaDesc = doc.selectFirst("meta[name=description]");
        if (metaDesc != null && StringUtils.hasText(metaDesc.attr("content"))) {
            return metaDesc.attr("content").trim();
        }
        Element ogDesc = doc.selectFirst("meta[property=og:description]");
        if (ogDesc != null && StringUtils.hasText(ogDesc.attr("content"))) {
            return ogDesc.attr("content").trim();
        }
        return "";
    }

    /**
     * 解析 Favicon 绝对路径
     */
    private static String parseFavicon(Document doc, String targetUrl) {
        // 尝试匹配常见的 icon 标签选择器
        String[] iconSelectors = {
                "link[rel~=(?i)^(shortcut|icon|apple-touch-icon)]",
                "link[rel=icon]",
                "link[rel=shortcut icon]",
                "link[rel=apple-touch-icon]"
        };

        for (String selector : iconSelectors) {
            Elements links = doc.select(selector);
            for (Element link : links) {
                String href = link.attr("href");
                if (StringUtils.hasText(href)) {
                    // 转为带域名的完整 HTTP/HTTPS 绝对路径
                    return makeAbsoluteUrl(targetUrl, href);
                }
            }
        }

        // 若 HTML 中未找到，兜底使用 domain/favicon.ico
        return getFallbackFavicon(targetUrl);
    }

    /**
     * 将相对路径（如 /favicon.ico 或 ./logo.png）转为完整的绝对 URL
     */
    private static String makeAbsoluteUrl(String baseUrl, String relativePath) {
        try {
            URI base = new URI(baseUrl);
            return base.resolve(relativePath).toString();
        } catch (Exception e) {
            return relativePath;
        }
    }

    /**
     * 默认域名下的 /favicon.ico 兜底路径
     */
    private static String getFallbackFavicon(String targetUrl) {
        try {
            URI uri = new URI(targetUrl);
            return uri.getScheme() + "://" + uri.getHost() + "/favicon.ico";
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 提取主域名作为默认标题
     */
    private static String getDomainName(String targetUrl) {
        try {
            return new URI(targetUrl).getHost();
        } catch (Exception e) {
            return targetUrl;
        }
    }
}