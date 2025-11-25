package com.progresssoft.fxdealmanager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<List<String>> handleAllExceptions(Exception e) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(List.of("Hey, I found a problem here! " + e.getMessage()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<List<String>> handleMaxSizeException(MaxUploadSizeExceededException e) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(List.of("Upload error: File is too large!"));
    }
}
