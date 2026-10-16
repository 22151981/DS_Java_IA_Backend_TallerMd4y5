# Especificación de Negocio - Sillón (SPEC.md)

## 1. Visión del Producto
SaaS multi-inquilino en Java (Spring Boot) para barberías y salones de belleza. Permite gestión de catálogo, estilistas, agenda de citas, pagos (Stripe) y funciones de IA (recordatorios y chatbot con Gemini vía HTTP RestClient).

## 2. Modelos y Límites de Negocio
- **Aislamiento Multi-inquilino:** Todo dato pertenece a un Salon. Un intento de acceso a datos de otro salón responde HTTP 404 (ocultamiento de existencia).
- **Plan GRATIS:** Máximo 2 estilistas y 10 créditos de IA iniciales.
- **Plan PRO:** Estilistas ilimitados, +300 créditos al activarse. Requiere suscripción Stripe.
- **Paquete de Créditos:** Pago único Stripe, +200 créditos de IA.
- **Reloj de Negocio:** Toda fecha ("hoy", "ahora") usa `America/Bogota` vía bean `Clock`.

## 3. Catálogo y Equipo
- Servicio: Duración entre 15 y 480 minutos. Precio entero mayor que 0.
- Estilista: Se vincula a uno o más servicios de su mismo salón.
- En plan GRATIS, intentar crear el 3er estilista responde HTTP 402.

## 4. Agenda y Reglas de Cita
- Estados: PENDIENTE, CONFIRMADA, COMPLETADA, CANCELADA, NO_ASISTIO.
- Ocupan agenda: PENDIENTE y CONFIRMADA.
- Transiciones válidas:
  - PENDIENTE -> CONFIRMADA o CANCELADA
  - CONFIRMADA -> COMPLETADA, CANCELADA o NO_ASISTIO
  - Finales: COMPLETADA, CANCELADA, NO_ASISTIO
- Horario: Dentro de apertura/cierre, sin domingos si el salón no abre, nunca en el pasado (409).
- Cruce de citas: Dos intervalos se cruzan si (inicio1 < fin2) y (fin1 > inicio2). Cita cruzada responde 409.
- Roles en agenda:
  - DUENO: ve todas las citas del salón y realiza cualquier transición permitida.
  - ESTILISTA: solo ve sus citas, solo puede pasarlas a COMPLETADA o NO_ASISTIO (otra cosa: 403; cita ajena: 404).

## 5. Pagos con Stripe
- Sesiones de checkout creadas desde backend (precios nunca enviados por el cliente).
- Si ya es PRO, pedir suscripción responde 409.
- Webhook: valida firma (400 si falla), idempotencia por `EventoStripe` (id ya procesado se ignora con 200 procesado=false).
- Eventos: `checkout.session.completed` y `customer.subscription.deleted`.

## 6. Inteligencia Artificial (Gemini vía HTTP RestClient)
- 1 crédito por respuesta. Si creditos == 0, responde 402 sin llamar a la IA.
- Recordatorio: solo para PENDIENTE o CONFIRMADA (otra: 409). Java calcula y formatea los datos verificados; la IA solo redacta el texto. Salida JSON estricta con clave `mensaje`.
- Chatbot: usa catálogo y horario real de la base de datos. Si piden agendar o cancelar, escala creando SolicitudContacto (motivo PEDIDO_POR_IA). Salida JSON con `respuesta` y `escalar`.
- Regla Anti-Promesas (Java): si la respuesta contiene "descuento", "cupón", "cupon", "gratis", "promoción", "promocion" o "reembols", se sustituye por mensaje seguro y se escala con motivo REGLA_DE_SEGURIDAD.

## 7. Formato Único de Errores (Todos los errores de la API)
JSON de 4 campos: `codigo`, `error`, `mensaje`, `fecha`.