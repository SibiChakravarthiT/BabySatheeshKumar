package com.poc.rag.rag_demo.service;

import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import org.apache.pdfbox.Loader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final EmbeddingService embeddingService;

    private final JdbcTemplate jdbcTemplate;

    @Value("${app.table-name:document_chunks}")
    private String tableName;


    /**
     * SELECT
     *  id,
     *  file_name,
     *  chunk_text
     * FROM document_chunks
     * ORDER BY embedding <=> ?::vector
     * LIMIT 3;
     */

    public void saveDocument(MultipartFile file) throws Exception {
        String text;
        String contentType = file.getContentType();
        if ("application/pdf".equals(contentType)) {
            text = readPdf(file);
        }
        else if ("text/plain".equals(contentType)) {
            text = new String(file.getBytes(), java.nio.charset.StandardCharsets.UTF_8);
        }
        else {
            throw new IllegalArgumentException("Unsupported file type. Only PDF and TXT files are supported");
        }


        List<String> chunks = chunkText(text);

        for(String chunk : chunks) {
            float[] vector = embeddingService.embed(chunk);
            String vectorString = Arrays.toString(vector);


            jdbcTemplate.update("""
                INSERT INTO"""
                    + " " + tableName + """
                (
                  file_name,
                  chunk_text,
                  embedding
                )
                VALUES
                (?, ?, ?::vector)
            """,
                    file.getOriginalFilename(),
                    chunk,
                    vectorString);
        }
    }



    public String readPdf(MultipartFile file) throws IOException {

        try (PDDocument doc = Loader.loadPDF(
                file.getBytes())) {

            return new PDFTextStripper().getText(doc);
        }
    }

    public List<String> chunkText(String text) {
        int chunkSize = 1000;
        int overlap = 200;

        List<String> chunks = new ArrayList<>();
        int start = 0;

        while(start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());
            chunks.add(text.substring(start,end));
            start += chunkSize-overlap;
        }

        return chunks;
    }
}
