package com.devsenior.sillon.publico;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record DisponibilidadRespuesta(
        LocalDate fecha,
        String servicio,
        String estilista,
        Integer duracionMinutos,
        List<LocalTime> horariosLibres
) {}
