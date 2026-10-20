package com.devsenior.sillon.compartido;

public class ExcepcionesNegocio {

    public static class RecursoNoEncontradoException extends RuntimeException {
        public RecursoNoEncontradoException(String mensaje) {
            super(mensaje);
        }
    }

    public static class ReglaNegocioException extends RuntimeException {
        public ReglaNegocioException(String mensaje) {
            super(mensaje);
        }
    }

    public static class LimitePlanException extends RuntimeException {
        public LimitePlanException(String mensaje) {
            super(mensaje);
        }
    }

    public static class AccionNoPermitidaException extends RuntimeException {
        public AccionNoPermitidaException(String mensaje) {
            super(mensaje);
        }
    }

    public static class CredencialesInvalidasException extends RuntimeException {
        public CredencialesInvalidasException(String mensaje) {
            super(mensaje);
        }
    }

    public static class FirmaInvalidaException extends RuntimeException {
        public FirmaInvalidaException(String mensaje) {
            super(mensaje);
        }
    }

    public static class PasarelaException extends RuntimeException {
        public PasarelaException(String mensaje) {
            super(mensaje);
        }
    }
}