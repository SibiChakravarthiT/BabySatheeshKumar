package com.poc.rag.rag_demo.exception;

import com.poc.rag.rag_demo.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PromptTooLongException.class)
    public ResponseEntity<ErrorResponse> handlePromptTooLong(
            PromptTooLongException ex) {

        log.error("Prompt too long", ex);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        "PROMPT_TOO_LONG",
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleFileTypeNotSupported(IllegalArgumentException ex) {

        log.error("Unsupported file type", ex);
        String code = "";
        if (ex.getMessage().contains("Unsupported file type")) {
            code = "UNSUPPORTED_FILE_TYPE";
        } else {
            code = "ILLEGAL_ARGUMENT";
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(code, ex.getMessage()
                ));


    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex) {

        log.error("Unexpected error", ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                        "INTERNAL_ERROR",
                        "Unexpected error occurred"
                ));
    }
}