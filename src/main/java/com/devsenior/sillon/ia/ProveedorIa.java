package com.devsenior.sillon.ia;

public interface ProveedorIa {
    String nombre();
    RespuestaIa generar(String instruccionesSistema, String mensajeUsuario);
}
