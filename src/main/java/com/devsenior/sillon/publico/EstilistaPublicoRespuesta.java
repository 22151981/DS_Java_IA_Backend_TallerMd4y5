package com.devsenior.sillon.publico;

import com.devsenior.sillon.catalogo.Servicio;
import com.devsenior.sillon.usuario.Usuario;
import java.util.List;

public record EstilistaPublicoRespuesta(
        Long id,
        String nombre,
        List<Long> servicioIds
) {
    public static EstilistaPublicoRespuesta desdeEntidad(Usuario usuario) {
        List<Long> ids = usuario.getServicios().stream()
                .filter(Servicio::getActivo)
                .map(Servicio::getId)
                .sorted()
                .toList();
        return new EstilistaPublicoRespuesta(usuario.getId(), usuario.getNombre(), ids);
    }
}
