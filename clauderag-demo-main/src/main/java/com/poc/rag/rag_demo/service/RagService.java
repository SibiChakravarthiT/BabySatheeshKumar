package com.poc.rag.rag_demo.service;

import com.anthropic.errors.BadRequestException;
import com.poc.rag.rag_demo.dto.AskResponse;
import com.poc.rag.rag_demo.exception.PromptTooLongException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tokenizer.TokenCountEstimator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagService {

    // pgvector's "<=>" operator returns COSINE DISTANCE (0 = identical, 2 = opposite).
    // We fetch a few extra candidates beyond top-K so the actual distance/similarity
    // of near-misses is visible in the logs, which makes it much easier to tune the
    // threshold below against your own documents.
    private static final int CANDIDATE_POOL_SIZE = 8;

    @Value("${app.input-token-limit:1000000}")
    private int maxInputToken; // for the model claude-sonnet-5
    private final EmbeddingService embeddingService;

    private final JdbcTemplate jdbcTemplate;

    private final ChatClient chatClient;

    private final TokenCountEstimator tokenCountEstimator;


    @Value("${app.table-name:document_chunks}")
    private String tableName;

    // Minimum cosine similarity (1 - cosine distance) a chunk must have to be treated
    // as relevant. Anything below this is dropped instead of being sent to the LLM,
    // which is what keeps the model from being handed unrelated context and
    // hallucinating an answer from it.
    @Value("${app.similarity-threshold:0.70}")
    private double similarityThreshold;

    // Max number of chunks actually sent to the chat model as context.
    @Value("${app.top-k:3}")
    private int topK;

    public AskResponse ask(String question) {

        log.info("Question : {}", question);
        if(question.length() < 5){
            return new AskResponse(
                    "Please provide a more specific question.",
                    null);
        }

        float[] queryEmbedding = embeddingService.embed(question);

        String embeddingVector = Arrays.toString(queryEmbedding);

        // Pull back a small candidate pool ordered by distance. We filter by
        // similarity threshold in Java (rather than in the WHERE clause) so that
        // rejected near-misses are still visible for logging/tuning.
        List<Map<String,Object>> candidates =
                jdbcTemplate.queryForList("""
                    SELECT chunk_text,
                           (embedding <=> ?::vector) AS distance
                    FROM"""
                        + " " + tableName + " " +
                    """
                    ORDER BY embedding <=> ?::vector
                    LIMIT ?
                """, embeddingVector, embeddingVector, CANDIDATE_POOL_SIZE);

        List<Map<String,Object>> rows = candidates.stream()
                .peek(r -> {
                    double distance = ((Number) r.get("distance")).doubleValue();
                    log.debug("Candidate chunk similarity={}, distance={}",
                            String.format("%.4f", 1 - distance),
                            String.format("%.4f", distance));
                })
                .filter(r -> (1 - ((Number) r.get("distance")).doubleValue()) >= similarityThreshold)
                .limit(topK)
                .collect(Collectors.toList());

        if(rows.isEmpty()) {
            log.info("No chunk met the similarity threshold {} for question: {}",
                    similarityThreshold, question);
            return new AskResponse(
                    "I couldn't find anything relevant to that question in the document.",
                    null
            );
        }

        String context =
                rows.stream()
                        .map(r ->
                                r.get("chunk_text")
                                        .toString())
                        .collect(Collectors.joining(
                                "\n\n"));


        String prompt = """
                You are a document assistant.

                Use ONLY the supplied context.

                Context:
                %s

                Question:
                %s

                If answer is not found,
                say:
                'Answer not found in document.'
                """
                .formatted(context,
                        question);



        int tokens = tokenCountEstimator.estimate(prompt);

        if (tokens > maxInputToken) {
            throw new PromptTooLongException("Input exceeds model limit.");
        }

        String answer = "";
        try {
            answer = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
        } catch (BadRequestException ex) {
            if (ex.getMessage() != null
                    && ex.getMessage().contains("prompt is too long")) {

                throw new PromptTooLongException(
                        "Input exceeds model limit.");
            }

            throw ex;
        }


        return new AskResponse(
                answer,
                rows.get(0)
                        .get("chunk_text")
                        .toString());
    }
}
