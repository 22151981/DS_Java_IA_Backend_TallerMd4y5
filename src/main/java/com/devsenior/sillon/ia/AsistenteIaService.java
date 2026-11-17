package com.devsenior.sillon.ia;

import com.devsenior.sillon.compartido.ExcepcionesNegocio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AsistenteIaService {
    private static final Logger logger = LoggerFactory.getLogger(AsistenteIaService.class);
    private final ProveedorIa openAi;
    private final ProveedorIa claude;

    public AsistenteIaService(ProveedorOpenAi openAi, ProveedorClaude claude) {
        this.openAi = openAi;
        this.claude = claude;
    }

    public RespuestaIa generar(String instruccionesSistema, String mensajeUsuario) {
        try {
            return openAi.generar(instruccionesSistema, mensajeUsuario);
        } catch (Exception openAiError) {
            logger.warn("Proveedor OpenAI falló; intentando Claude", openAiError);
        }
        try {
            return claude.generar(instruccionesSistema, mensajeUsuario);
        } catch (Exception claudeError) {
            logger.error("Todos los proveedores de IA fallaron", claudeError);
            throw new ExcepcionesNegocio.ServicioIaNoDisponibleException("Servicio de IA no disponible");
        }
    }
}
