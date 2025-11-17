package com.context.memorybook.domain.ai.dto;

import com.context.memorybook.common.enums.ContentType;
import lombok.Data;

@Data
public class ShareContentRequest {
    private ContentType type;
    private String title;
    private String description;
    private String textContent; // For articles, blogs, news
    private String url; // For links, images, videos
    private String source; // Source website/author
}

