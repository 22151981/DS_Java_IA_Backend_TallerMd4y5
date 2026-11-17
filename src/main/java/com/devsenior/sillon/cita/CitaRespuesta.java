package com.devsenior.sillon.cita;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CitaRespuesta(
        Long id,
        LocalDateTime inicio,
        LocalDateTime fin,
        EstadoCita estado,
        OrigenCita origen,
        BigDecimal precio,
        Long servicioId,
        String servicioNombre,
        Long estilistaId,
        String estilistaNombre,
        String clienteNombre,
        String clienteTelefono,
        String nota,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion
) {
    public static CitaRespuesta desdeEntidad(Cita c) {
        return new CitaRespuesta(c.getId(), c.getInicio(), c.getFin(), c.getEstado(), c.getOrigen(),
                c.getPrecio(), c.getServicio().getId(), c.getServicio().getNombre(),
                c.getEstilista().getId(), c.getEstilista().getNombre(), c.getCliente().getNombre(),
                c.getCliente().getTelefono(), c.getNota(), c.getFechaCreacion(), c.getFechaActualizacion());
    }
}
