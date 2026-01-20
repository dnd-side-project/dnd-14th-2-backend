package com.example.demo.infrastructure.advice;

import com.example.demo.infrastructure.advice.dto.ErrorResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.OffsetDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String STATUS_HEADER = "X-HTTP-Status";

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        String msg = e.getMessage();
        if (msg == null || msg.isBlank()) {
            msg = "요청 값이 올바르지 않습니다.";
        }
        return error(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        return error(HttpStatus.BAD_REQUEST, validationMessage(e));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        return error(HttpStatus.BAD_REQUEST, "요청 형식이 올바르지 않습니다.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
    }

    private ResponseEntity<ErrorResponse> error(HttpStatus status, String message) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(STATUS_HEADER, String.valueOf(status.value()));

        ErrorResponse body = new ErrorResponse(
                message,
                OffsetDateTime.now().toString()
        );

        return new ResponseEntity<>(body, headers, status);
    }

    private String validationMessage(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        if (fieldError == null) {
            return "요청 값이 올바르지 않습니다.";
        }

        String msg = fieldError.getDefaultMessage();
        if (msg == null || msg.isBlank()) {
            msg = "요청 값이 올바르지 않습니다.";
        }

        return fieldError.getField() + ": " + msg;
    }
}