package com.poc.rag.rag_demo.repository;

import com.poc.rag.rag_demo.entity.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChunkRepository
        extends JpaRepository<DocumentChunk, Long> {
}
