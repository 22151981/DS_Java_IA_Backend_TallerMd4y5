package com.devsenior.sillon.publico;

import java.util.List;

public record SalonPublicoRespuesta(
        String nombre,
        String slug,
        String ciudad,
        String direccion,
        String telefono,
        String horario,
        List<ServicioPublicoRespuesta> servicios,
        List<EstilistaPublicoRespuesta> estilistas
) {}
