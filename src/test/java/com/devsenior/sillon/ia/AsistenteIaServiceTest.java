package com.devsenior.sillon.ia;

import com.devsenior.sillon.compartido.ExcepcionesNegocio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsistenteIaServiceTest {
    @Mock ProveedorOpenAi openAi; @Mock ProveedorClaude claude;

    @Test void fallbackCuandoPrincipalFalla() {
        RespuestaIa esperada = new RespuestaIa("{\"ok\":true}", null);
        when(openAi.generar("sys","msg")).thenThrow(new RuntimeException("fallo"));
        when(claude.generar("sys","msg")).thenReturn(esperada);
        assertEquals(esperada, new AsistenteIaService(openAi,claude).generar("sys","msg"));
    }
    @Test void ambosFallanLanzo503() {
        when(openAi.generar(anyString(),anyString())).thenThrow(new RuntimeException());
        when(claude.generar(anyString(),anyString())).thenThrow(new RuntimeException());
        assertThrows(ExcepcionesNegocio.ServicioIaNoDisponibleException.class,
                () -> new AsistenteIaService(openAi,claude).generar("sys","msg"));
    }
}
