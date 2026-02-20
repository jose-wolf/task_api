package com.josewolf.task_api.exceptions;

import com.josewolf.task_api.dto.StandardError;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalHandlerException{

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<StandardError> handleResourceNotFound(ResourceNotFoundException ex) {
        StandardError err = new StandardError(
                HttpStatus.NOT_FOUND.value(),
                "Não encontrado",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<StandardError> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        StandardError err = new StandardError(
                HttpStatus.CONFLICT.value(),
                "Conflito de dados",
                ex.getMessage()
        );
        return  ResponseEntity.status(HttpStatus.CONFLICT).body(err);
    }


}
