package com.poc.rag.rag_demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response returned by the RAG engine")
public class AskResponse {

    @Schema(description = "Generated answer")
    private String answer;

    @Schema(description = "Source text chunk used to generate the answer")
    private String sourceChunk;
}
