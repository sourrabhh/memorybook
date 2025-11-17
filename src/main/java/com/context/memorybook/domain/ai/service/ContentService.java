package com.context.memorybook.domain.ai.service;

import com.context.memorybook.common.enums.ContentType;
import com.context.memorybook.domain.ai.model.Content;
import com.context.memorybook.domain.ai.repository.ContentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ContentService {

    @Autowired
    private ContentRepository contentRepository;

    @Transactional
    public Content saveContent(Content content) {
        content.setCreatedAt(LocalDateTime.now());
        content.setUpdatedAt(LocalDateTime.now());
        return contentRepository.save(content);
    }

    @Transactional(readOnly = true)
    public List<Content> getContentByUser(Long userId) {
        return contentRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<Content> getContentByUserAndType(Long userId, ContentType type) {
        return contentRepository.findByUserIdAndType(userId, type);
    }

    @Transactional(readOnly = true)
    public List<Content> searchContent(Long userId, String query) {
        return contentRepository.searchContent(userId, query);
    }

    @Transactional(readOnly = true)
    public Content getContentById(Long id) {
        return contentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Content not found"));
    }
}

