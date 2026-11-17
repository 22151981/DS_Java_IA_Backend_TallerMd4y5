package com.devsenior.sillon.ia;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class ProveedorOpenAi implements ProveedorIa {
    private final String url;
    private final String clave;
    private final String modelo;
    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ProveedorOpenAi(@Value("${sillon.ia.openai.url}") String url,
                           @Value("${sillon.ia.openai.clave:llave_dummy}") String clave,
                           @Value("${sillon.ia.openai.modelo:gemini-2.5-flash}") String modelo) {
        this.url = url;
        this.clave = clave;
        this.modelo = modelo;
        this.restClient = RestClient.builder().build();
    }

    @Override public String nombre() { return "openai"; }

    @Override
    public RespuestaIa generar(String instruccionesSistema, String mensajeUsuario) {
        if (clave == null || clave.isBlank() || "llave_dummy".equals(clave)) {
            throw new RuntimeException("Proveedor OpenAI sin clave configurada");
        }
        long inicio = System.nanoTime();
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", modelo);
            body.put("response_format", Map.of("type", "json_object"));
            body.put("messages", List.of(
                    Map.of("role", "system", "content", instruccionesSistema),
                    Map.of("role", "user", "content", mensajeUsuario)));
            String json = restClient.post().uri(url)
                    .header("Authorization", "Bearer " + clave)
                    .header("Content-Type", "application/json")
                    .body(body).retrieve().body(String.class);
            JsonNode root = objectMapper.readTree(json);
            JsonNode choice = root.path("choices").path(0);
            String contenido = choice.path("message").path("content").asText("");
            JsonNode uso = root.path("usage");
            int entrada = uso.path("prompt_tokens").asInt(0);
            int salida = uso.path("completion_tokens").asInt(0);
            long ms = (System.nanoTime() - inicio) / 1_000_000;
            return new RespuestaIa(contenido, new FacturaIa(nombre(), modelo, entrada, salida, 0.0, ms));
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("Error al consumir el proveedor OpenAI", ex);
        }
    }
}
