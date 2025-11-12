package com.context.memorybook.service;

import com.context.memorybook.models.Memory;
import com.context.memorybook.repository.MemoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MemoryService {
    @Autowired
    private MemoryRepository memoryRepository;

    public Memory addMemory(Memory memory){
        memory.setCreatedAt(LocalDateTime.now());
        memory.setUpdatedAt(LocalDateTime.now());
        return memoryRepository.save(memory);
    }

//    public List<Memory> getMemoriesByuser(Long userId){
//        return memoryRepository.findByUserId(userId);
//    }
}
