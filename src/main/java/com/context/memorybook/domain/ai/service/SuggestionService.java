package com.context.memorybook.domain.ai.service;

import com.context.memorybook.domain.memory.model.Memory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SuggestionService {

    @Autowired
    private ContextExtractionService contextExtractionService;

    /**
     * Generate suggestions based on memory context and related content
     */
    public String generateSuggestions(Memory memory, List<String> relatedContentTitles) {
        StringBuilder suggestions = new StringBuilder();

        if (memory.getRelevanceScore() > 1) {
            suggestions.append("This topic has been referenced ")
                    .append(memory.getRelevanceScore())
                    .append(" times. ");
        }

        if (relatedContentTitles != null && !relatedContentTitles.isEmpty()) {
            suggestions.append("Related content you've shared: ");
            relatedContentTitles.stream()
                    .limit(3)
                    .forEach(title -> suggestions.append("'").append(title).append("', "));
            
            // Remove last comma and space
            if (suggestions.length() > 2) {
                suggestions.setLength(suggestions.length() - 2);
            }
            suggestions.append(". ");
        }

        // Add context-based suggestions
        if (memory.getKeywords() != null && !memory.getKeywords().isEmpty()) {
            suggestions.append("Consider exploring more about: ")
                    .append(memory.getKeywords().substring(0, Math.min(50, memory.getKeywords().length())))
                    .append(".");
        }

        return suggestions.toString().trim();
    }

    /**
     * Generate or update summary for a memory
     */
    public String generateSummary(Memory memory, String newContent) {
        String existingContent = memory.getContent() != null ? memory.getContent() : "";
        String combinedContent = existingContent.isEmpty() ? newContent 
                : existingContent + "\n\n" + newContent;
        
        // Generate summary from combined content
        return contextExtractionService.generateSummary(combinedContent, 200);
    }
}

