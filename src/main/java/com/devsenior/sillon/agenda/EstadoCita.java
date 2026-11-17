package com.devsenior.sillon.agenda;

public enum EstadoCita {
    PENDIENTE,
    CONFIRMADA,
    COMPLETADA,
    CANCELADA,
    NO_ASISTIO;

    public boolean ocupaAgenda() {
        return this == PENDIENTE || this == CONFIRMADA;
    }

    public boolean esFinal() {
        return this == COMPLETADA || this == CANCELADA || this == NO_ASISTIO;
    }

    public boolean puedeTransicionarA(EstadoCita nuevoEstado) {
        if (this == PENDIENTE) {
            return nuevoEstado == CONFIRMADA || nuevoEstado == CANCELADA;
        }
        if (this == CONFIRMADA) {
            return nuevoEstado == COMPLETADA || nuevoEstado == CANCELADA || nuevoEstado == NO_ASISTIO;
        }
        return false;
    }
}