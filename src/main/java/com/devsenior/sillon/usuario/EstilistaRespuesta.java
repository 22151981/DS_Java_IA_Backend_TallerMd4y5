package com.devsenior.sillon.usuario;

import com.devsenior.sillon.catalogo.Servicio;
import java.util.List;

public record EstilistaRespuesta(
        Long id,
        String nombre,
        String email,
        Boolean activo,
        List<String> servicios
) {
    public static EstilistaRespuesta desdeEntidad(Usuario usuario) {
        List<String> nombresServicios = usuario.getServicios() == null
                ? List.of()
                : usuario.getServicios().stream()
                .map(Servicio::getNombre)
                .toList();
        return new EstilistaRespuesta(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getActivo(),
                nombresServicios
        );
    }
}