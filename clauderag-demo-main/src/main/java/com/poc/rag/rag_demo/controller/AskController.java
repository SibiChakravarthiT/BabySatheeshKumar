package com.poc.rag.rag_demo.controller;

import com.poc.rag.rag_demo.dto.AskResponse;
import com.poc.rag.rag_demo.service.RagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AskController {

    private final RagService ragService;

    @Operation(
            summary = "Ask a question",
            description = "Queries the RAG system and returns the generated answer."
    )
    @ApiResponse(responseCode = "200", description = "Answer generated successfully")
    @GetMapping("/ask")
    public AskResponse ask(
            @Parameter(
                    description = "Question to ask the RAG engine",
                    example = "What is Spring AI?")
            @RequestParam String q) {

        return ragService.ask(q);
    }
}
