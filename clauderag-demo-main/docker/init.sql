CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS document_chunks_claude_llm
(
    id BIGSERIAL PRIMARY KEY,

    file_name VARCHAR(255),

    chunk_text TEXT,

    embedding VECTOR(768)
);