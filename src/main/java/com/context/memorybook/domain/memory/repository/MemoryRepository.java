package com.context.memorybook.domain.memory.repository;

import com.context.memorybook.domain.memory.model.Memory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemoryRepository extends JpaRepository<Memory, Long> {
    // Get all memories for a specific user
    List<Memory> findByUserId(Long userId);

    // Get memories for a specific user and context (case-insensitive)
    List<Memory> findByUserIdAndContextIgnoreCase(Long userId, String context);

    // Search memories by keyword (in content or context)
    @Query("SELECT m FROM Memory m WHERE m.userId = :userId AND " +
            "(LOWER(m.content) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(m.context) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(m.keywords) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Memory> searchByContentOrContext(Long userId, String query);

    // Find memories by keywords
    @Query("SELECT m FROM Memory m WHERE m.userId = :userId AND " +
            "LOWER(m.keywords) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Memory> findByKeyword(Long userId, String keyword);
}
