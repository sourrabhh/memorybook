package com.context.memorybook.domain.memory.service;

import com.context.memorybook.domain.ai.model.Content;
import com.context.memorybook.domain.memory.model.Memory;
import com.context.memorybook.domain.memory.model.Tag;
import com.context.memorybook.domain.memory.repository.MemoryRepository;
import com.context.memorybook.domain.memory.repository.TagRepository;
import com.context.memorybook.domain.ai.service.ContentService;
import com.context.memorybook.domain.ai.service.ContextExtractionService;
import com.context.memorybook.domain.ai.service.SuggestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MemoryService {
    @Autowired
    private MemoryRepository memoryRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private MemoryMatchingService memoryMatchingService;

    @Autowired
    private ContextExtractionService contextExtractionService;

    @Autowired
    private SuggestionService suggestionService;

    @Autowired
    private ContentService contentService;

    @Transactional
    public Memory addMemory(Memory memory){
        Set<Tag> resolvedTags = new HashSet<>();

        if (memory.getTags() != null) {
            for(Tag tag : memory.getTags()){
                if (tag != null && tag.getName() != null) {
                    Tag existingTag = tagRepository.findByNameIgnoreCase(tag.getName())
                            .orElseGet(() -> tagRepository.save(new Tag(tag.getName())));
                    resolvedTags.add(existingTag);
                }
            }
        }
        memory.setTags(resolvedTags);
        
        // Extract keywords if not provided
        if (memory.getKeywords() == null || memory.getKeywords().isEmpty()) {
            String textForKeywords = (memory.getContext() != null ? memory.getContext() : "") + 
                                    " " + (memory.getContent() != null ? memory.getContent() : "");
            List<String> keywords = contextExtractionService.extractKeywords(textForKeywords);
            memory.setKeywords(String.join(", ", keywords));
        }
        
        // Set default relevance score
        if (memory.getRelevanceScore() == null) {
            memory.setRelevanceScore(1);
        }
        
        memory.setCreatedAt(LocalDateTime.now());
        memory.setUpdatedAt(LocalDateTime.now());

        return memoryRepository.save(memory);
    }

    /**
     * Smart memory creation/update based on shared content
     * This is the core feature: finds similar memory or creates new one
     */
    @Transactional
    public Memory createOrUpdateMemoryFromContent(Long userId, Content content) {
        String extractedContext = content.getExtractedContext();
        if (extractedContext == null || extractedContext.isEmpty()) {
            extractedContext = contextExtractionService.extractContext(
                    content.getTitle(), content.getDescription(), content.getTextContent()
            );
            content.setExtractedContext(extractedContext);
        }

        // Find similar memory
        Memory similarMemory = memoryMatchingService.findMostSimilarMemory(userId, extractedContext);

        if (similarMemory != null) {
            // Update existing memory with new content
            return updateMemoryWithContent(similarMemory.getId(), userId, content);
        } else {
            // Create new memory
            Memory newMemory = new Memory();
            newMemory.setUserId(userId);
            newMemory.setContext(extractedContext);
            newMemory.setContent(content.getTitle() + "\n" + 
                               (content.getDescription() != null ? content.getDescription() : "") + "\n" +
                               (content.getTextContent() != null ? content.getTextContent() : ""));
            newMemory.setType("Content");
            
            // Extract keywords
            List<String> keywords = contextExtractionService.extractKeywords(
                    content.getTitle() + " " + 
                    (content.getDescription() != null ? content.getDescription() : "") + " " +
                    (content.getTextContent() != null ? content.getTextContent() : "")
            );
            newMemory.setKeywords(String.join(", ", keywords));
            
            // Generate summary
            String summary = contextExtractionService.generateSummary(
                    newMemory.getContent(), 200
            );
            newMemory.setSummary(summary);
            
            // Set related content ID
            newMemory.setRelatedContentIds(String.valueOf(content.getId()));
            newMemory.setRelevanceScore(1);
            newMemory.setLastRelatedContentAt(LocalDateTime.now());
            
            Memory savedMemory = addMemory(newMemory);
            
            // Generate suggestions
            String suggestions = suggestionService.generateSuggestions(savedMemory, 
                    Collections.singletonList(content.getTitle()));
            savedMemory.setSuggestions(suggestions);
            
            return memoryRepository.save(savedMemory);
        }
    }

    /**
     * Update existing memory with new related content
     */
    @Transactional
    public Memory updateMemoryWithContent(Long memoryId, Long userId, Content content) {
        Memory memory = memoryRepository.findById(memoryId)
                .orElseThrow(() -> new RuntimeException("Memory not found"));
        
        // Authorization check
        if (!memory.getUserId().equals(userId)) {
            throw new SecurityException("Not authorized to update this memory");
        }

        // Update content with new information
        String newContent = content.getTitle() + "\n" + 
                           (content.getDescription() != null ? content.getDescription() : "") + "\n" +
                           (content.getTextContent() != null ? content.getTextContent() : "");
        
        String updatedContent = memory.getContent() != null 
                ? memory.getContent() + "\n\n---\n\n" + newContent
                : newContent;
        
        memory.setContent(updatedContent);
        
        // Update context with merged information
        String newContext = contextExtractionService.extractContext(
                content.getTitle(), content.getDescription(), content.getTextContent()
        );
        memory.setContext(memory.getContext() + ", " + newContext);
        
        // Update keywords
        List<String> existingKeywords = memory.getKeywords() != null 
                ? Arrays.asList(memory.getKeywords().split(",\\s*"))
                : new ArrayList<>();
        List<String> newKeywords = contextExtractionService.extractKeywords(
                content.getTitle() + " " + 
                (content.getDescription() != null ? content.getDescription() : "") + " " +
                (content.getTextContent() != null ? content.getTextContent() : "")
        );
        Set<String> mergedKeywords = new HashSet<>(existingKeywords);
        mergedKeywords.addAll(newKeywords);
        memory.setKeywords(String.join(", ", mergedKeywords));
        
        // Update related content IDs
        String relatedIds = memory.getRelatedContentIds() != null 
                ? memory.getRelatedContentIds() + "," + content.getId()
                : String.valueOf(content.getId());
        memory.setRelatedContentIds(relatedIds);
        
        // Increment relevance score
        memory.setRelevanceScore(memory.getRelevanceScore() + 1);
        memory.setLastRelatedContentAt(LocalDateTime.now());
        
        // Regenerate summary
        String summary = suggestionService.generateSummary(memory, newContent);
        memory.setSummary(summary);
        
        // Get related content titles for suggestions
        List<String> relatedTitles = getRelatedContentTitles(memory);
        String suggestions = suggestionService.generateSuggestions(memory, relatedTitles);
        memory.setSuggestions(suggestions);
        
        memory.setUpdatedAt(LocalDateTime.now());
        return memoryRepository.save(memory);
    }

    /**
     * Get titles of related content
     */
    private List<String> getRelatedContentTitles(Memory memory) {
        if (memory.getRelatedContentIds() == null || memory.getRelatedContentIds().isEmpty()) {
            return Collections.emptyList();
        }
        
        return Arrays.stream(memory.getRelatedContentIds().split(","))
                .map(String::trim)
                .filter(id -> !id.isEmpty())
                .map(id -> {
                    try {
                        Content content = contentService.getContentById(Long.parseLong(id));
                        return content.getTitle();
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Memory> getMemoriesByUser(Long userId) {
        return memoryRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<Memory> getMemoriesByContext(Long userId, String context) {
        return memoryRepository.findByUserIdAndContextIgnoreCase(userId, context);
    }

    @Transactional(readOnly = true)
    public List<Memory> searchMemories(Long userId, String query) {
        return memoryRepository.searchByContentOrContext(userId, query);
    }

    @Transactional
    public Memory updateMemory(Long id, Long userId, Memory updatedMemory) {
        return memoryRepository.findById(id).map(memory -> {
            // Authorization check: verify memory belongs to the user
            if (!memory.getUserId().equals(userId)) {
                throw new SecurityException("Not authorized to update this memory");
            }
            
            memory.setContent(updatedMemory.getContent());
            memory.setContext(updatedMemory.getContext());
            memory.setType(updatedMemory.getType());
            
            // Update tags if provided
            if (updatedMemory.getTags() != null) {
                Set<Tag> resolvedTags = new HashSet<>();
                for(Tag tag : updatedMemory.getTags()){
                    if (tag != null && tag.getName() != null) {
                        Tag existingTag = tagRepository.findByNameIgnoreCase(tag.getName())
                                .orElseGet(() -> tagRepository.save(new Tag(tag.getName())));
                        resolvedTags.add(existingTag);
                    }
                }
                memory.setTags(resolvedTags);
            }
            
            memory.setUpdatedAt(LocalDateTime.now());
            return memoryRepository.save(memory);
        }).orElseThrow(() -> new RuntimeException("Memory not found"));
    }

    @Transactional
    public void deleteMemory(Long id, Long userId) {
        Memory memory = memoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Memory not found"));
        
        // Authorization check: verify memory belongs to the user
        if (!memory.getUserId().equals(userId)) {
            throw new SecurityException("Not authorized to delete this memory");
        }
        
        memoryRepository.deleteById(id);
    }
}
