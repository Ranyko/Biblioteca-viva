package br.com.bibliotecaviva.exception;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadable(
            HttpMessageNotReadableException exception, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Dados inválidos",
                "Confira o JSON, os tipos dos campos e o perfil informado", request, Map.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleIntegrity(
            DataIntegrityViolationException exception, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "Conflito",
                "A operação conflita com dados existentes ou restrições do cadastro", request, Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        Map<String, String> campos = new LinkedHashMap<>();
        for (FieldError erro : exception.getBindingResult().getFieldErrors()) {
            campos.putIfAbsent(erro.getField(), erro.getDefaultMessage());
        }
        return build(HttpStatus.BAD_REQUEST, "Dados inválidos", "Revise os campos informados", request, campos);
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<ApiError> handleCredentials(
            CredenciaisInvalidasException exception,
            HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "Não autorizado", exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(UsuarioInativoException.class)
    public ResponseEntity<ApiError> handleInactive(
            UsuarioInativoException exception,
            HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, "Acesso negado", exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<ApiError> handleConflict(
            ConflitoException exception,
            HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "Conflito", exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ApiError> handleNotFound(
            RecursoNaoEncontradoException exception,
            HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "Não encontrado", exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(
            IllegalArgumentException exception,
            HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Dados inválidos", exception.getMessage(), request, Map.of());
    }

    private ResponseEntity<ApiError> build(
            HttpStatus status,
            String erro,
            String mensagem,
            HttpServletRequest request,
            Map<String, String> campos) {
        ApiError body = new ApiError(
                OffsetDateTime.now(),
                status.value(),
                erro,
                mensagem,
                request.getRequestURI(),
                campos);
        return ResponseEntity.status(status).body(body);
    }
}
