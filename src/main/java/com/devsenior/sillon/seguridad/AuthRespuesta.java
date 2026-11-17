package com.devsenior.sillon.seguridad;

import com.devsenior.sillon.usuario.Rol;

public record AuthRespuesta(
        String token,
        String tipo,
        Long expiraEnMinutos,
        Long usuarioId,
        String nombre,
        Rol rol,
        Long salonId,
        String salonSlug
) {
}