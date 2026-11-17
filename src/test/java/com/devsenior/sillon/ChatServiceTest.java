package com.devsenior.sillon;

import org.junit.jupiter.api.Test;

class ChatServiceTest {
    @Test
    void preguntaNormalSinEscalar() {
        boolean resultado = false;
        org.junit.jupiter.api.Assertions.assertFalse(resultado);
    }

    @Test
    void preguntaConEscalamiento() {
        boolean resultado = true;
        org.junit.jupiter.api.Assertions.assertTrue(resultado);
    }

    @Test
    void reglaAntiPromesas() {
        String respuesta = "Con gusto un miembro de nuestro equipo te atenderá personalmente";
        org.junit.jupiter.api.Assertions.assertFalse(respuesta.toLowerCase().contains("descuento"));
    }
}
