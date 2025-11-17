package com.context.memorybook.domain.memory.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "memories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Memory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String context;

    private String type;  // optional: Note, Event, Reminder

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String summary; // AI-generated summary of the memory

    @Column(columnDefinition = "TEXT")
    private String suggestions; // Suggestions based on the memory context

    @Column(columnDefinition = "TEXT")
    private String relatedContentIds; // Comma-separated IDs of related content

    @Column(nullable = false)
    private Integer relevanceScore = 1; // How many times related content was shared

    @Column(columnDefinition = "TEXT")
    private String keywords; // Extracted keywords for matching

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
    private LocalDateTime lastRelatedContentAt; // When was the last related content shared

    @ManyToMany(fetch = FetchType.EAGER, cascade = {
            CascadeType.PERSIST, CascadeType.MERGE
    })
    @JoinTable(
            name = "memory_tags",
            joinColumns = @JoinColumn(name = "memory_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
