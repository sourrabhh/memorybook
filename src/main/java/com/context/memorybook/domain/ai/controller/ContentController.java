package com.context.memorybook.domain.ai.controller;

import com.context.memorybook.domain.user.model.User;
import com.context.memorybook.domain.ai.dto.ShareContentRequest;
import com.context.memorybook.domain.ai.dto.ShareContentResponse;
import com.context.memorybook.common.enums.ContentType;
import com.context.memorybook.infrastructure.security.UserPrincipal;
import com.context.memorybook.domain.ai.model.Content;
import com.context.memorybook.domain.memory.model.Memory;
import com.context.memorybook.domain.ai.service.ContentService;
import com.context.memorybook.domain.ai.service.ContextExtractionService;
import com.context.memorybook.domain.memory.service.MemoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/content")
public class ContentController {

    @Autowired
    private ContentService contentService;

    @Autowired
    private MemoryService memoryService;

    @Autowired
    private ContextExtractionService contextExtractionService;

    /**
     * Share content (article, blog, news, link, etc.)
     * This is the main endpoint that handles smart memory creation/updating
     */
    @PostMapping("/share")
    public ResponseEntity<?> shareContent(
            @RequestBody ShareContentRequest request,
            Authentication authentication) {
        try {
            // Get user ID from authentication
            Long userId = getUserIdFromAuthentication(authentication);

            // Create Content entity
            Content content = new Content();
            content.setUserId(userId);
            content.setType(request.getType());
            content.setTitle(request.getTitle());
            content.setDescription(request.getDescription());
            content.setTextContent(request.getTextContent());
            content.setUrl(request.getUrl());
            content.setSource(request.getSource());

            // Extract context from content
            String extractedContext = contextExtractionService.extractContext(
                    request.getTitle(),
                    request.getDescription(),
                    request.getTextContent()
            );
            content.setExtractedContext(extractedContext);

            // Extract keywords
            String fullText = (request.getTitle() != null ? request.getTitle() : "") + " " +
                            (request.getDescription() != null ? request.getDescription() : "") + " " +
                            (request.getTextContent() != null ? request.getTextContent() : "");
            List<String> keywords = contextExtractionService.extractKeywords(fullText);
            content.setKeywords(String.join(", ", keywords));

            // Save content first
            Content savedContent = contentService.saveContent(content);

            // Use smart memory service to create or update memory
            // This will automatically find similar memories and update or create new one
            Memory memory = memoryService.createOrUpdateMemoryFromContent(userId, savedContent);
            
            // Determine if it's a new memory by checking relevance score
            boolean isNewMemory = (memory.getRelevanceScore() == 1 && 
                                   memory.getRelatedContentIds() != null && 
                                   memory.getRelatedContentIds().equals(String.valueOf(savedContent.getId())));

            // Create response
            ShareContentResponse response = new ShareContentResponse();
            response.setContentId(savedContent.getId());
            response.setMemory(memory);
            response.setNewMemory(isNewMemory);
            response.setExtractedContext(extractedContext);
            response.setSuggestions(memory.getSuggestions());
            response.setSummary(memory.getSummary());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to process content: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get all shared content for the authenticated user
     */
    @GetMapping("/my-content")
    public ResponseEntity<List<Content>> getMyContent(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        List<Content> contents = contentService.getContentByUser(userId);
        return ResponseEntity.ok(contents);
    }

    /**
     * Get content by type
     */
    @GetMapping("/my-content/type/{type}")
    public ResponseEntity<List<Content>> getContentByType(
            @PathVariable ContentType type,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        List<Content> contents = contentService.getContentByUserAndType(userId, type);
        return ResponseEntity.ok(contents);
    }

    /**
     * Search content
     */
    @GetMapping("/my-content/search")
    public ResponseEntity<List<Content>> searchContent(
            @RequestParam String query,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        List<Content> contents = contentService.searchContent(userId, query);
        return ResponseEntity.ok(contents);
    }

    /**
     * Helper method to get user ID from authentication
     */
    private Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new SecurityException("User not authenticated");
        }
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        UserPrincipal userPrincipal =
                (UserPrincipal) userDetails;
        
        // Access the User object from UserPrincipal
        User user = userPrincipal.getUser();
        
        if (user == null) {
            throw new SecurityException("User not found");
        }
        
        return user.getId();
    }
}

