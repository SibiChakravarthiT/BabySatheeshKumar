package com.poc.rag.rag_demo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final EmbeddingModel embeddingModel;

    public float[] embed(String text) {

        EmbeddingResponse response =
                embeddingModel.call(
                        new EmbeddingRequest(
                                List.of(text),
                                null));

        return response
                .getResults()
                .get(0)
                .getOutput();
    }
}
