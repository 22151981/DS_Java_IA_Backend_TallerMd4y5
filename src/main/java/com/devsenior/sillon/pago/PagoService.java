package com.devsenior.sillon.pago;

import com.devsenior.sillon.compartido.ExcepcionesNegocio;
import com.devsenior.sillon.salon.Plan;
import com.devsenior.sillon.salon.Salon;
import com.devsenior.sillon.salon.SalonRepository;
import com.devsenior.sillon.usuario.UsuarioRepository;
import com.stripe.StripeClient;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class PagoService {

    private static final Logger logger = LoggerFactory.getLogger(PagoService.class);

    private final RestClient restClient;
    private final SalonRepository salonRepository;
    private final UsuarioRepository usuarioRepository;
    private final EventoStripeRepository eventoStripeRepository;
    private final Clock clock;
    private final String precioPro;
    private final String precioCreditos;
    private final String secretoWebhook;
    private final String urlPublica;
    private final String urlApi;
    private final String claveSecreta;
    private final ObjectMapper objectMapper;

    public PagoService(SalonRepository salonRepository,
                       UsuarioRepository usuarioRepository,
                       EventoStripeRepository eventoStripeRepository,
                       Clock clock,
                       @Value("${stripe.precio.pro}") String precioPro,
                       @Value("${stripe.precio.creditos}") String precioCreditos,
                       @Value("${stripe.secreto.webhook}") String secretoWebhook,
                       @Value("${sillon.url.publica}") String urlPublica,
                       @Value("${stripe.url.api}") String urlApi,
                       @Value("${stripe.clave.secreta}") String claveSecreta) {
        this.restClient = RestClient.builder().build();
        this.salonRepository = salonRepository;
        this.usuarioRepository = usuarioRepository;
        this.eventoStripeRepository = eventoStripeRepository;
        this.clock = clock;
        this.precioPro = precioPro;
        this.precioCreditos = precioCreditos;
        this.secretoWebhook = secretoWebhook;
        this.urlPublica = urlPublica;
        this.urlApi = quitarBarraFinal(urlApi);
        this.claveSecreta = claveSecreta;
        this.objectMapper = new ObjectMapper();
    }

    public SesionPagoRespuesta crearSesionSuscripcion(Long salonId, String emailUsuario) {
        Salon salon = salonRepository.findById(salonId)
                .orElseThrow(() -> new ExcepcionesNegocio.RecursoNoEncontradoException("Salón no encontrado"));
        if (salon.getPlan() == Plan.PRO) {
            throw new ExcepcionesNegocio.ReglaNegocioException("El salón ya tiene el plan PRO");
        }

        return crearSesion(salonId, emailUsuario, TipoCompra.SUSCRIPCION_PRO, precioPro, true);
    }

    public SesionPagoRespuesta crearSesionCreditos(Long salonId, String emailUsuario) {
        salonRepository.findById(salonId)
                .orElseThrow(() -> new ExcepcionesNegocio.RecursoNoEncontradoException("Salón no encontrado"));

        return crearSesion(salonId, emailUsuario, TipoCompra.PAQUETE_CREDITOS, precioCreditos, false);
    }

    private SesionPagoRespuesta crearSesion(Long salonId, String emailUsuario, TipoCompra tipo,
                                            String precio, boolean suscripcion) {
        Map<String, String> formulario = new java.util.LinkedHashMap<>();
        formulario.put("mode", suscripcion ? "subscription" : "payment");
        formulario.put("customer_email", emailUsuario);
        formulario.put("success_url", urlPublica + "/api/pagos/resultado?estado=exito");
        formulario.put("cancel_url", urlPublica + "/api/pagos/resultado?estado=cancelado");
        formulario.put("client_reference_id", String.valueOf(salonId));
        formulario.put("line_items[0][price]", precio);
        formulario.put("line_items[0][quantity]", "1");
        formulario.put("metadata[salonId]", String.valueOf(salonId));
        formulario.put("metadata[tipoCompra]", tipo.name());
        if (suscripcion) {
            formulario.put("subscription_data[metadata][salonId]", String.valueOf(salonId));
            formulario.put("subscription_data[metadata][tipoCompra]", tipo.name());
        }

        String cuerpo = formulario.entrySet().stream()
                .map(e -> codificar(e.getKey()) + "=" + codificar(e.getValue()))
                .reduce((a, b) -> a + "&" + b)
                .orElse("");

        try {
            String respuesta = restClient.post()
                    .uri(urlApi + "/v1/checkout/sessions")
                    .header("Authorization", "Bearer " + claveSecreta)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(cuerpo)
                    .retrieve()
                    .body(String.class);
            JsonNode json = objectMapper.readTree(respuesta);
            return new SesionPagoRespuesta(tipo, json.path("id").asText(), json.path("url").asText());
        } catch (RuntimeException ex) {
            logger.error("Error Stripe al crear la sesión: {}", ex.getMessage(), ex);
            throw new ExcepcionesNegocio.PasarelaException("Error al crear la sesión de pago");
        } catch (Exception ex) {
            logger.error("Error al interpretar la respuesta de Stripe: {}", ex.getMessage(), ex);
            throw new ExcepcionesNegocio.PasarelaException("Error al crear la sesión de pago");
        }
    }

    private String quitarBarraFinal(String valor) {
        if (valor == null) return "";
        return valor.endsWith("/") ? valor.substring(0, valor.length() - 1) : valor;
    }

    private String codificar(String valor) {
        try {
            return java.net.URLEncoder.encode(valor, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Error al codificar parámetros de Stripe", ex);
        }
    }

    @Transactional
    public WebhookRespuesta procesarWebhook(String payload, String firmaHeader) {
        if (firmaHeader == null || firmaHeader.isBlank()) {
            throw new ExcepcionesNegocio.FirmaInvalidaException("Firma de Stripe ausente");
        }

        Event event;
        try {
            event = Webhook.constructEvent(payload, firmaHeader, secretoWebhook);
        } catch (SignatureVerificationException ex) {
            throw new ExcepcionesNegocio.FirmaInvalidaException("Firma de Stripe inválida");
        } catch (Exception ex) {
            throw new ExcepcionesNegocio.FirmaInvalidaException("Firma de Stripe inválida");
        }

        if (eventoStripeRepository.existsById(event.getId())) {
            return new WebhookRespuesta(true, false);
        }

        try {
            JsonNode root = objectMapper.readTree(payload);
            JsonNode dataObject = root.path("data").path("object");

            if ("checkout.session.completed".equals(event.getType())) {
                String estadoPago = dataObject.path("payment_status").asText(null);
                if (estadoPago == null || estadoPago.isBlank() || "paid".equalsIgnoreCase(estadoPago)) {
                    JsonNode metadata = dataObject.path("metadata");
                    String salonIdTexto = metadata.path("salonId").asText(null);
                    if (salonIdTexto == null || salonIdTexto.isBlank()) {
                        salonIdTexto = metadata.path("salon_id").asText(null);
                    }
                    if (salonIdTexto == null || salonIdTexto.isBlank()) {
                        salonIdTexto = dataObject.path("client_reference_id").asText(null);
                    }

                    String tipoCompraTexto = metadata.path("tipoCompra").asText(null);
                    if (tipoCompraTexto == null || tipoCompraTexto.isBlank()) {
                        tipoCompraTexto = metadata.path("tipo_compra").asText(null);
                    }
                    if (tipoCompraTexto == null || tipoCompraTexto.isBlank()) {
                        String mode = dataObject.path("mode").asText();
                        if ("subscription".equals(mode)) {
                            tipoCompraTexto = "SUSCRIPCION_PRO";
                        } else if ("payment".equals(mode)) {
                            tipoCompraTexto = "PAQUETE_CREDITOS";
                        }
                    }

                    if (salonIdTexto != null && tipoCompraTexto != null) {
                        try {
                            Long salonId = Long.valueOf(salonIdTexto);
                            Salon salon = salonRepository.findById(salonId)
                                    .orElseThrow(() -> new ExcepcionesNegocio.RecursoNoEncontradoException("Salón no encontrado"));
                            if ("SUSCRIPCION_PRO".equals(tipoCompraTexto)) {
                                salon.setPlan(Plan.PRO);
                                salon.setStripeSuscripcionId(dataObject.path("subscription").asText(null));
                                salon.setCreditosIa(salon.getCreditosIa() == null ? 300 : salon.getCreditosIa() + 300);
                            } else if ("PAQUETE_CREDITOS".equals(tipoCompraTexto)) {
                                salon.setCreditosIa(salon.getCreditosIa() == null ? 200 : salon.getCreditosIa() + 200);
                            }
                            salonRepository.saveAndFlush(salon);
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            }

            if ("customer.subscription.deleted".equals(event.getType())) {
                String subId = dataObject.path("id").asText(null);
                if (subId != null) {
                    salonRepository.findByStripeSuscripcionId(subId).ifPresent(salon -> {
                        salon.setPlan(Plan.GRATIS);
                        salon.setStripeSuscripcionId(null);
                        salonRepository.saveAndFlush(salon);
                    });
                }
            }
        } catch (Exception ex) {
            throw new ExcepcionesNegocio.PasarelaException("Error al interpretar el evento de Stripe");
        }

        EventoStripe evento = new EventoStripe();
        evento.setId(event.getId());
        evento.setTipo(event.getType());
        evento.setFechaProcesado(LocalDateTime.now(clock));
        eventoStripeRepository.save(evento);
        return new WebhookRespuesta(true, true);
    }
}
