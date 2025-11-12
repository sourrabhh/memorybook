package com.context.memorybook.repository;

import com.context.memorybook.models.Memory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemoryRepository extends JpaRepository<Memory, Long> {

}
