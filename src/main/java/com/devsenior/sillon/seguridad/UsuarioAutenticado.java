package com.devsenior.sillon.seguridad;

public record UsuarioAutenticado(
        String email,
        Long salonId,
        String rol
) {
}