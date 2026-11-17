package com.devsenior.sillon.publico;

import com.devsenior.sillon.catalogo.Servicio;
import java.math.BigDecimal;

public record ServicioPublicoRespuesta(
        Long id,
        String nombre,
        String descripcion,
        Integer duracionMinutos,
        BigDecimal precio
) {
    public static ServicioPublicoRespuesta desdeEntidad(Servicio servicio) {
        return new ServicioPublicoRespuesta(
                servicio.getId(), servicio.getNombre(), servicio.getDescripcion(),
                servicio.getDuracionMinutos(), servicio.getPrecio());
    }
}
