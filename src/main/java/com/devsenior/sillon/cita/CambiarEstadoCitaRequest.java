package com.devsenior.sillon.cita;

import jakarta.validation.constraints.NotNull;

public record CambiarEstadoCitaRequest(@NotNull EstadoCita estado) {}
