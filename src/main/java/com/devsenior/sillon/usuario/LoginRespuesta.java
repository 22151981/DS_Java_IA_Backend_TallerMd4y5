package com.devsenior.sillon.usuario;

public record LoginRespuesta(
        String token,
        String email,
        Rol rol,
        Long salonId
) {
}