package com.devsenior.sillon.salon;

import java.time.LocalDateTime;

public record SalonRespuesta(
        Long id,
        String nombre,
        String slug,
        String ciudad,
        String direccion,
        String telefono,
        Plan plan,
        Integer creditosIa,
        ConfiguracionSalon configuracion,
        LocalDateTime fechaCreacion
) {
    public static SalonRespuesta desdeEntidad(Salon salon) {
        return new SalonRespuesta(
                salon.getId(),
                salon.getNombre(),
                salon.getSlug(),
                salon.getCiudad(),
                salon.getDireccion(),
                salon.getTelefono(),
                salon.getPlan(),
                salon.getCreditosIa(),
                salon.getConfiguracion() == null ? ConfiguracionSalon.porDefecto() : salon.getConfiguracion(),
                salon.getFechaCreacion()
        );
    }
}