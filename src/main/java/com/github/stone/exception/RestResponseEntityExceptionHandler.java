package com.github.stone.exception;

import com.github.stone.controller.GenericResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.stream.Collectors;

import static com.github.stone.controller.GenericResponse.ERROR_CODE;

@ControllerAdvice
@Slf4j
public class RestResponseEntityExceptionHandler {

    @ExceptionHandler
    protected ResponseEntity<GenericResponse> handle(DataIntegrityViolationException ex) {
        log.error("error", ex);
        // java.sql.SQLIntegrityConstraintViolationException: Column 'name' cannot be null
        // org.hibernate.PropertyValueException: not-null property references a null or transient value : com.github.stone.entity.Todo.name
        return ResponseEntity.ok(new GenericResponse(ERROR_CODE, "system error", "throw by " + ex.getClass()));
    }

    @ExceptionHandler
    protected ResponseEntity<GenericResponse> handle(MethodArgumentNotValidException ex) {
        log.error("error", ex);
        String errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> "[" + fieldError.getField() + "] " + fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return ResponseEntity.ok(new GenericResponse(ERROR_CODE, errors, "throw by " + ex.getClass()));
    }

    @ExceptionHandler
    protected ResponseEntity<GenericResponse> handle(ConstraintViolationException ex) {
        log.error("error", ex);
        String errors = ex.getConstraintViolations()
                .stream()
                .map(fieldError ->  "[" + fieldError.getPropertyPath() + "] " + fieldError.getMessage())
                .collect(Collectors.joining(", "));

        return ResponseEntity.ok(new GenericResponse(ERROR_CODE, errors, "throw by " + ex.getClass()));
    }


}
