package com.context.memorybook.domain.ai.dto;

import com.context.memorybook.domain.memory.model.Memory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShareContentResponse {
    private Long contentId;
    private Memory memory; // Can be new or updated memory
    private boolean isNewMemory; // true if new memory was created, false if existing was updated
    private String extractedContext;
    private String suggestions;
    private String summary;
}

