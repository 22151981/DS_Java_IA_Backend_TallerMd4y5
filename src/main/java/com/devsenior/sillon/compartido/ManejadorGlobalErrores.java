package com.devsenior.sillon.compartido;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ManejadorGlobalErrores {

    private final Clock clock;

    public ManejadorGlobalErrores(Clock clock) {
        this.clock = clock;
    }

    private ResponseEntity<ErrorRespuesta> construirRespuesta(HttpStatus status, String mensaje) {
        ErrorRespuesta error = new ErrorRespuesta(
                status.value(),
                status.getReasonPhrase(),
                mensaje,
                LocalDateTime.now(clock)
        );
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(ExcepcionesNegocio.RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorRespuesta> manejar404(ExcepcionesNegocio.RecursoNoEncontradoException ex) {
        return construirRespuesta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ExcepcionesNegocio.ReglaNegocioException.class)
    public ResponseEntity<ErrorRespuesta> manejar409(ExcepcionesNegocio.ReglaNegocioException ex) {
        return construirRespuesta(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(ExcepcionesNegocio.LimitePlanException.class)
    public ResponseEntity<ErrorRespuesta> manejar402(ExcepcionesNegocio.LimitePlanException ex) {
        return construirRespuesta(HttpStatus.PAYMENT_REQUIRED, ex.getMessage());
    }

    @ExceptionHandler(ExcepcionesNegocio.AccionNoPermitidaException.class)
    public ResponseEntity<ErrorRespuesta> manejar403(ExcepcionesNegocio.AccionNoPermitidaException ex) {
        return construirRespuesta(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(ExcepcionesNegocio.CredencialesInvalidasException.class)
    public ResponseEntity<ErrorRespuesta> manejar401(ExcepcionesNegocio.CredencialesInvalidasException ex) {
        return construirRespuesta(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(ExcepcionesNegocio.FirmaInvalidaException.class)
    public ResponseEntity<ErrorRespuesta> manejarFirma400(ExcepcionesNegocio.FirmaInvalidaException ex) {
        return construirRespuesta(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(ExcepcionesNegocio.PasarelaException.class)
    public ResponseEntity<ErrorRespuesta> manejar502(ExcepcionesNegocio.PasarelaException ex) {
        return construirRespuesta(HttpStatus.BAD_GATEWAY, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRespuesta> manejarValidacion(MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return construirRespuesta(HttpStatus.BAD_REQUEST, mensaje);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorRespuesta> manejarJsonMalFormado(HttpMessageNotReadableException ex) {
        return construirRespuesta(HttpStatus.BAD_REQUEST, "Cuerpo JSON mal formado");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorRespuesta> manejarParametroFaltante(MissingServletRequestParameterException ex) {
        return construirRespuesta(HttpStatus.BAD_REQUEST, "Parámetro requerido faltante: " + ex.getParameterName());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorRespuesta> manejarTipoInvalido(MethodArgumentTypeMismatchException ex) {
        return construirRespuesta(HttpStatus.BAD_REQUEST, "Tipo de parámetro inválido: " + ex.getName());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRespuesta> manejarGeneral(Exception ex) {
        return construirRespuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor");
    }
}