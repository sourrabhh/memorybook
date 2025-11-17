package com.context.memorybook.domain.ai.repository;

import com.context.memorybook.domain.ai.model.Content;
import com.context.memorybook.common.enums.ContentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long> {
    // Get all content for a specific user
    List<Content> findByUserId(Long userId);

    // Get content by type for a user
    List<Content> findByUserIdAndType(Long userId, ContentType type);

    // Search content by keywords or context
    @Query("SELECT c FROM Content c WHERE c.userId = :userId AND " +
            "(LOWER(c.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(c.description) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(c.textContent) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(c.extractedContext) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Content> searchContent(Long userId, String query);

    // Find content by keywords
    @Query("SELECT c FROM Content c WHERE c.userId = :userId AND " +
            "LOWER(c.keywords) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Content> findByKeyword(Long userId, String keyword);
}

