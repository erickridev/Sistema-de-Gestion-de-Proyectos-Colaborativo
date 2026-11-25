
package org.esfe.exceptions;

import org.esfe.dtos.ErrorResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.dao.DataIntegrityViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // CREAR RESPUESTA DE ERROR
    private ResponseEntity<ErrorResponse> crearRespuesta(
            HttpStatusCode status,
            String mensaje
    ) {

        ErrorResponse error = new ErrorResponse(
                status.value(),
                mensaje,
                LocalDateTime.now()
        );

        return ResponseEntity.status(status).body(error);
    }

    // ERROR DE AUTENTICACION
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(
            AuthenticationException ex
    ) {

        return crearRespuesta(
                HttpStatus.UNAUTHORIZED,
                "Credenciales invalidas"
        );
    }

    // ERROR DE PERMISOS
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex
    ) {

        return crearRespuesta(
                HttpStatus.FORBIDDEN,
                "No tiene permisos para realizar esta accion"
        );
    }

    // ERRORES HTTP CONTROLADOS
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(
            ResponseStatusException ex
    ) {

        String mensaje = ex.getReason();

        if (mensaje == null || mensaje.isBlank()) {
            mensaje = "No se pudo completar la solicitud";
        }

        return crearRespuesta(
                ex.getStatusCode(),
                mensaje
        );
    }

    // ERRORES DE VALIDACION
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex
    ) {

        String mensaje = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error ->
                        error.getField() + ": " +
                                error.getDefaultMessage()
                )
                .distinct()
                .collect(Collectors.joining("; "));

        if (mensaje.isBlank()) {
            mensaje = "Los datos enviados no son validos";
        }

        return crearRespuesta(
                HttpStatus.BAD_REQUEST,
                mensaje
        );
    }

    // ARGUMENTOS INVALIDOS
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex
    ) {

        return crearRespuesta(
                HttpStatus.BAD_REQUEST,
                "Los datos proporcionados no son validos"
        );
    }

    // ERRORES DE INTEGRIDAD EN MYSQL
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(
            DataIntegrityViolationException ex
    ) {

        return crearRespuesta(
                HttpStatus.CONFLICT,
                "La operacion no se puede completar por una "
                        + "restriccion de integridad de datos"
        );
    }

    @ExceptionHandler({
            org.springframework.http.converter.HttpMessageNotReadableException.class,
            org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,
            org.springframework.web.bind.MissingServletRequestParameterException.class
    })
    public ResponseEntity<ErrorResponse> handleMalformedRequest(Exception ex) {
        return crearRespuesta(HttpStatus.BAD_REQUEST, "El cuerpo o los parametros de la solicitud no son validos");
    }

    // Mantener los codigos y encabezados HTTP definidos por Spring (405, 415, 406, 404...).
    @ExceptionHandler(org.springframework.web.ErrorResponseException.class)
    public ResponseEntity<ErrorResponse> handleHttpError(org.springframework.web.ErrorResponseException ex) {
        return ResponseEntity.status(ex.getStatusCode()).headers(ex.getHeaders())
                .body(new ErrorResponse(ex.getStatusCode().value(), "No se pudo completar la solicitud HTTP", LocalDateTime.now()));
    }

    @ExceptionHandler({
            org.springframework.web.HttpRequestMethodNotSupportedException.class,
            org.springframework.web.HttpMediaTypeNotSupportedException.class,
            org.springframework.web.HttpMediaTypeNotAcceptableException.class,
            org.springframework.web.servlet.resource.NoResourceFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleHttpRequest(Exception ex) {
        org.springframework.web.ErrorResponse error = (org.springframework.web.ErrorResponse) ex;
        return ResponseEntity.status(error.getStatusCode()).headers(error.getHeaders())
                .body(new ErrorResponse(error.getStatusCode().value(), "No se pudo completar la solicitud HTTP", LocalDateTime.now()));
    }

    // ERRORES INTERNOS
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex
    ) {

        logger.error("Error interno no controlado", ex);

        return crearRespuesta(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrio un error interno en el servidor"
        );
    }
}
