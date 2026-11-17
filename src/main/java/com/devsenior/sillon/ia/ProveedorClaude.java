package com.devsenior.sillon.ia;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class ProveedorClaude implements ProveedorIa {
    private static final Logger logger = LoggerFactory.getLogger(ProveedorClaude.class);
    private final String url;
    private final String clave;
    private final String modelo;
    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ProveedorClaude(@Value("${sillon.ia.claude.url}") String url,
                           @Value("${sillon.ia.claude.clave:}") String clave,
                           @Value("${sillon.ia.claude.modelo:claude-3-5-haiku-latest}") String modelo) {
        this.url = url;
        this.clave = clave;
        this.modelo = modelo;
        this.restClient = RestClient.builder().build();
    }

    @Override public String nombre() { return "claude"; }

    @Override
    public RespuestaIa generar(String instruccionesSistema, String mensajeUsuario) {
        if (clave == null || clave.isBlank()) {
            logger.error("Proveedor Claude no disponible: clave no configurada");
            throw new RuntimeException("Proveedor Claude sin clave configurada");
        }
        long inicio = System.nanoTime();
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", modelo);
            body.put("max_tokens", 700);
            body.put("system", instruccionesSistema);
            body.put("messages", List.of(Map.of("role", "user", "content", mensajeUsuario)));
            String json = restClient.post().uri(url)
                    .header("x-api-key", clave)
                    .header("anthropic-version", "2023-06-01")
                    .header("Content-Type", "application/json")
                    .body(body).retrieve().body(String.class);
            JsonNode root = objectMapper.readTree(json);
            StringBuilder contenido = new StringBuilder();
            for (JsonNode parte : root.path("content")) {
                if ("text".equals(parte.path("type").asText())) contenido.append(parte.path("text").asText());
            }
            JsonNode uso = root.path("usage");
            int entrada = uso.path("input_tokens").asInt(0);
            int salida = uso.path("output_tokens").asInt(0);
            long ms = (System.nanoTime() - inicio) / 1_000_000;
            return new RespuestaIa(contenido.toString(), new FacturaIa(nombre(), modelo, entrada, salida, 0.0, ms));
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("Error al consumir el proveedor Claude", ex);
        }
    }
}
