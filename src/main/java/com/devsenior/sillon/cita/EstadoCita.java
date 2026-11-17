package com.devsenior.sillon.cita;

public enum EstadoCita {
    PENDIENTE,
    CONFIRMADA,
    COMPLETADA,
    CANCELADA,
    NO_ASISTIO;

    public boolean ocupaAgenda() {
        return this == PENDIENTE || this == CONFIRMADA;
    }

    public boolean puedeTransicionarA(EstadoCita destino) {
        if (destino == null) return false;
        if (this == PENDIENTE) {
            return destino == CONFIRMADA || destino == CANCELADA;
        }
        if (this == CONFIRMADA) {
            return destino == COMPLETADA || destino == CANCELADA || destino == NO_ASISTIO;
        }
        return false;
    }
}
