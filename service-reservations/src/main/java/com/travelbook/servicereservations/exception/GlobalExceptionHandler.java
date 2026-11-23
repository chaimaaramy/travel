package com.travelbook.servicereservations.exception;

import feign.FeignException;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ServiceExterneIndisponibleException.class)
    public ResponseEntity<ProblemDetail> serviceExterneIndisponible(
            ServiceExterneIndisponibleException ex) {
        return problem(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ProblemDetail> responseStatus(ResponseStatusException ex) {
        String detail = ex.getReason() != null ? ex.getReason() : ex.getStatusCode().toString();
        return ResponseEntity.status(ex.getStatusCode())
                .body(ProblemDetail.forStatusAndDetail(ex.getStatusCode(), detail));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> validation(MethodArgumentNotValidException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Requête invalide");
        problem.setProperty("errors", ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .toList());
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> bodyIllisible(HttpMessageNotReadableException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Corps de requête illisible");
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ProblemDetail> headerManquant(MissingRequestHeaderException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Header manquant : " + ex.getHeaderName());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ProblemDetail> parametreManquant(MissingServletRequestParameterException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Paramètre manquant : " + ex.getParameterName());
    }

    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<ProblemDetail> tropDeRequetes(RequestNotPermitted ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header(HttpHeaders.RETRY_AFTER, "1")
                .body(ProblemDetail.forStatusAndDetail(
                        HttpStatus.TOO_MANY_REQUESTS, "Trop de requêtes"));
    }

    @ExceptionHandler(CallNotPermittedException.class)
    public ResponseEntity<ProblemDetail> circuitOuvert(CallNotPermittedException ex) {
        return problem(HttpStatus.SERVICE_UNAVAILABLE, "Service distant momentanément indisponible");
    }

    @ExceptionHandler(BulkheadFullException.class)
    public ResponseEntity<ProblemDetail> bulkheadPlein(BulkheadFullException ex) {
        return problem(HttpStatus.SERVICE_UNAVAILABLE, "Service distant momentanément saturé");
    }

    @ExceptionHandler(FeignException.NotFound.class)
    public ResponseEntity<ProblemDetail> feignNotFound(FeignException.NotFound ex) {
        return problem(HttpStatus.NOT_FOUND, "Ressource distante introuvable");
    }

    @ExceptionHandler(FeignException.Conflict.class)
    public ResponseEntity<ProblemDetail> feignConflict(FeignException.Conflict ex) {
        return problem(HttpStatus.CONFLICT, "Conflit distant");
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ProblemDetail> feignErreur(FeignException ex) {
        return problem(HttpStatus.SERVICE_UNAVAILABLE, "Service distant indisponible");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> conflitDonnees(DataIntegrityViolationException ex) {
        return problem(HttpStatus.CONFLICT, "Conflit de données");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> erreurInterne(Exception ex) {
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur interne");
    }

    private ResponseEntity<ProblemDetail> problem(HttpStatus status, String detail) {
        return ResponseEntity.status(status)
                .body(ProblemDetail.forStatusAndDetail(status, detail));
    }

    private String formatFieldError(FieldError error) {
        String message = error.getDefaultMessage();
        return error.getField() + (message != null ? " : " + message : "");
    }
}
