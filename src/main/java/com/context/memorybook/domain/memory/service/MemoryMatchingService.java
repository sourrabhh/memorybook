package com.context.memorybook.domain.memory.service;

import com.context.memorybook.domain.memory.model.Memory;
import com.context.memorybook.domain.memory.repository.MemoryRepository;
import com.context.memorybook.domain.ai.service.ContextExtractionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MemoryMatchingService {

    @Autowired
    private MemoryRepository memoryRepository;

    @Autowired
    private ContextExtractionService contextExtractionService;

    private static final double SIMILARITY_THRESHOLD = 0.3; // Minimum similarity to consider matching

    /**
     * Find similar memories based on context
     * Returns memories sorted by relevance score and similarity
     */
    @Transactional(readOnly = true)
    public List<Memory> findSimilarMemories(Long userId, String context) {
        List<Memory> userMemories = memoryRepository.findByUserId(userId);
        
        if (userMemories.isEmpty()) {
            return List.of();
        }

        // Calculate similarity for each memory and filter
        return userMemories.stream()
                .map(memory -> {
                    double similarity = contextExtractionService.calculateSimilarity(
                            context, 
                            memory.getContext() + " " + (memory.getKeywords() != null ? memory.getKeywords() : "")
                    );
                    return new MemorySimilarity(memory, similarity);
                })
                .filter(ms -> ms.similarity >= SIMILARITY_THRESHOLD)
                .sorted(Comparator
                        .comparing((MemorySimilarity ms) -> ms.similarity).reversed()
                        .thenComparing(ms -> ms.memory.getRelevanceScore()).reversed())
                .map(ms -> ms.memory)
                .collect(Collectors.toList());
    }

    /**
     * Find the most similar memory
     */
    @Transactional(readOnly = true)
    public Memory findMostSimilarMemory(Long userId, String context) {
        List<Memory> similarMemories = findSimilarMemories(userId, context);
        return similarMemories.isEmpty() ? null : similarMemories.get(0);
    }

    /**
     * Helper class to store memory with similarity score
     */
    private static class MemorySimilarity {
        Memory memory;
        double similarity;

        MemorySimilarity(Memory memory, double similarity) {
            this.memory = memory;
            this.similarity = similarity;
        }
    }
}

