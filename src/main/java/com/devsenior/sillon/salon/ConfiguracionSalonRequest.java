package com.devsenior.sillon.salon;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

public record ConfiguracionSalonRequest(
        @NotNull(message = "La hora de apertura es requerida")
        LocalTime horaApertura,

        @NotNull(message = "La hora de cierre es requerida")
        LocalTime horaCierre,

        @NotNull(message = "Debe indicar si abre domingos")
        Boolean abreDomingos,

        @NotNull(message = "El intervalo en minutos es requerido")
        Integer intervaloMinutos
) {
}