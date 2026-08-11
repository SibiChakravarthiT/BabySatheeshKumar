package com.poc.rag.rag_demo.exception;

public class PromptTooLongException extends RuntimeException {

    public PromptTooLongException(String message) {
        super(message);
    }
}
