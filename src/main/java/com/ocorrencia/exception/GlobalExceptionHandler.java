package com.ocorrencia.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public
    ResponseEntity<?> handleRuntimeException(RuntimeException e) {
        // Retorna um erro 400 com a mensagem da exceção lançada no Service
        return ResponseEntity.badRequest().body(Map.of(
                "erro", "Regra de Negócio Violada",
                "mensagem", e.getMessage()
        ));
    }
}
