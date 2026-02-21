package com.example.demo.infrastructure.advice;

import com.example.demo.application.exception.UnauthorizedException;
import com.example.demo.infrastructure.advice.dto.ErrorResponse;
import java.time.OffsetDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String STATUS_HEADER = "X-HTTP-Status";

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        String msg = e.getMessage();
        if (msg == null || msg.isBlank()) {
            msg = "요청 값이 올바르지 않습니다.";
        }
        return error(HttpStatus.BAD_REQUEST, msg);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException e) {
        String msg = e.getMessage();
        if (msg == null || msg.isBlank()) {
            msg = "요청 값이 올바르지 않습니다.";
        }
        return error(HttpStatus.INTERNAL_SERVER_ERROR, msg);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String message = validationMessage(e);
        return error(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException e) {
        return error(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        return error(HttpStatus.BAD_REQUEST, "요청 형식이 올바르지 않습니다.");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
        MissingServletRequestParameterException e
    ) {
        String message = "필수 요청 파라미터가 누락되었습니다: " + e.getParameterName();
        return error(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        String message = "요청 파라미터 형식이 올바르지 않습니다: " + e.getName();
        return error(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
        // 가장 구체적인 원인 찾기
        Throwable rootCause = NestedExceptionUtils.getMostSpecificCause(e);

        log.error("[ERROR] status={} | exception={} | message={}",
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            rootCause.getClass().getSimpleName(),
            rootCause.getMessage(),
            e);

        return error(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");
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
            return "요청 값이 올바르지 않습니다.";
        }

        return msg;
    }
}
