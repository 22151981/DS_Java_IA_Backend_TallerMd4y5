package com.devsenior.sillon.cliente;

public record ClienteRespuesta(Long id, String nombre, String telefono) {
    public static ClienteRespuesta desdeEntidad(Cliente cliente) {
        return new ClienteRespuesta(cliente.getId(), cliente.getNombre(), cliente.getTelefono());
    }
}
