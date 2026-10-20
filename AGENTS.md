# Instrucciones para el Agente (AGENTS.md) - Proyecto Sillón

Eres un asistente técnico senior para el proyecto "Sillón" en Java 17 y Spring Boot 4.x.
Sigue estrictamente las reglas aquí descritas. NO inventes librerías ni te saltes el contrato.

## 1. Reglas Técnicas Obligatorias
- PROHIBIDO LOMBOK: Escribe constructores, getters y setters explícitos o usa Java Records para DTOs.
- PROHIBIDO SPRING AI Y SDKs OFICIALES DE IA: Toda comunicación con Gemini/Claude se realiza vía HTTP usando `RestClient`.
- GOBERNANZA DE FECHAS: Nunca uses `LocalDateTime.now()` directo sin pasar por el bean `Clock` configurado con `America/Bogota`.
- URLs CONFIGURABLES:
  - La URL base de Stripe debe leerse de la variable `STRIPE_URL_API` (default: `https://api.stripe.com`).
  - La URL base del proveedor OpenAI/Gemini debe leerse de `sillon.ia.openai.url` (variable `SILLON_IA_OPENAI_URL`).
  - Nunca dejes URLs externas fijas (hardcoded) en el código.
- SEGURIDAD Y MULTI-TENANT:
  - Todo repositorio filtra por `salonId`.
  - Si un recurso no existe o pertenece a otro salón, responde 404.
  - La ruta `/error` de Spring Boot DEBE ser pública en `SecurityFilterChain` para no convertir excepciones en falsos 401.

## 2. Formato de Respuestas y Errores
- Todos los errores HTTP (400, 401, 402, 403, 404, 409, 502, 503) deben ser manejados por `@RestControllerAdvice` retornando la clase de error estándar con 4 campos: `codigo`, `error`, `mensaje`, `fecha`.
- Nombres de campos en DTOs y JSONs deben coincidir exactamente con el contrato del proyecto.

## 3. Pruebas y Modificaciones
- NUNCA modifiques un test para que pase. Si un test falla, revisa el código contra `SPEC.md`.
- No agregues dependencias al `pom.xml` sin aprobación expresa.