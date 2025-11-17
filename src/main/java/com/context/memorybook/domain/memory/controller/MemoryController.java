package com.context.memorybook.domain.memory.controller;

import com.context.memorybook.domain.memory.model.Memory;
import com.context.memorybook.domain.user.model.User;
import com.context.memorybook.domain.memory.service.MemoryService;
import com.context.memorybook.infrastructure.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/memories")
public class MemoryController {

    @Autowired
    private MemoryService memoryService;

    /**
     * Get all memories for the authenticated user
     */
    @GetMapping
    public ResponseEntity<List<Memory>> getMyMemories(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        List<Memory> memories = memoryService.getMemoriesByUser(userId);
        return ResponseEntity.ok(memories);
    }

    /**
     * Get a specific memory by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getMemory(@PathVariable Long id, Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            Memory memory = memoryService.getMemoriesByUser(userId).stream()
                    .filter(m -> m.getId().equals(id))
                    .findFirst()
                    .orElse(null);

            if (memory == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Memory not found");
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(memory);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Search memories by query
     */
    @GetMapping("/search")
    public ResponseEntity<List<Memory>> searchMemories(
            @RequestParam String query,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        List<Memory> memories = memoryService.searchMemories(userId, query);
        return ResponseEntity.ok(memories);
    }

    /**
     * Get memories by context
     */
    @GetMapping("/context/{context}")
    public ResponseEntity<List<Memory>> getMemoriesByContext(
            @PathVariable String context,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        List<Memory> memories = memoryService.getMemoriesByContext(userId, context);
        return ResponseEntity.ok(memories);
    }

    /**
     * Create a new memory manually
     */
    @PostMapping
    public ResponseEntity<?> createMemory(@RequestBody Memory memory, Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            memory.setUserId(userId);
            Memory createdMemory = memoryService.addMemory(memory);
            return ResponseEntity.ok(createdMemory);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create memory: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Update an existing memory
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateMemory(
            @PathVariable Long id,
            @RequestBody Memory updatedMemory,
            Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            Memory memory = memoryService.updateMemory(id, userId, updatedMemory);
            return ResponseEntity.ok(memory);
        } catch (SecurityException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(403).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update memory: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Delete a memory
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMemory(@PathVariable Long id, Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            memoryService.deleteMemory(id, userId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Memory deleted successfully");
            return ResponseEntity.ok(response);
        } catch (SecurityException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(403).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete memory: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
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

        User user = userPrincipal.getUser();

        if (user == null) {
            throw new SecurityException("User not found");
        }

        return user.getId();
    }
}

