package com.context.memorybook.domain.ai.model;

import com.context.memorybook.common.enums.ContentType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "contents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Content {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContentType type;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String textContent; // For articles, blogs, news

    @Column(columnDefinition = "TEXT")
    private String url; // For links, images, videos

    @Column(columnDefinition = "TEXT")
    private String source; // Source website/author

    @Column(columnDefinition = "TEXT")
    private String extractedContext; // Context extracted from content

    @Column(columnDefinition = "TEXT")
    private String keywords; // Extracted keywords (comma-separated)

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

