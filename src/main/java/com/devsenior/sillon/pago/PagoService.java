package com.devsenior.sillon.pago;

import com.devsenior.sillon.compartido.ExcepcionesNegocio;
import com.devsenior.sillon.salon.Plan;
import com.devsenior.sillon.salon.Salon;
import com.devsenior.sillon.salon.SalonRepository;
import com.devsenior.sillon.usuario.UsuarioRepository;
import com.stripe.StripeClient;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class PagoService {

    private static final Logger logger = LoggerFactory.getLogger(PagoService.class);

    private final StripeClient stripeClient;
    private final SalonRepository salonRepository;
    private final UsuarioRepository usuarioRepository;
    private final EventoStripeRepository eventoStripeRepository;
    private final Clock clock;
    private final String precioPro;
    private final String precioCreditos;
    private final String secretoWebhook;
    private final String urlPublica;

    public PagoService(StripeClient stripeClient,
                       SalonRepository salonRepository,
                       UsuarioRepository usuarioRepository,
                       EventoStripeRepository eventoStripeRepository,
                       Clock clock,
                       @Value("${stripe.precio.pro}") String precioPro,
                       @Value("${stripe.precio.creditos}") String precioCreditos,
                       @Value("${stripe.secreto.webhook}") String secretoWebhook,
                       @Value("${sillon.url.publica}") String urlPublica) {
        this.stripeClient = stripeClient;
        this.salonRepository = salonRepository;
        this.usuarioRepository = usuarioRepository;
        this.eventoStripeRepository = eventoStripeRepository;
        this.clock = clock;
        this.precioPro = precioPro;
        this.precioCreditos = precioCreditos;
        this.secretoWebhook = secretoWebhook;
        this.urlPublica = urlPublica;
    }

    public SesionPagoRespuesta crearSesionSuscripcion(Long salonId, String emailUsuario) {
        Salon salon = salonRepository.findById(salonId)
                .orElseThrow(() -> new ExcepcionesNegocio.RecursoNoEncontradoException("Salón no encontrado"));
        if (salon.getPlan() == Plan.PRO) {
            throw new ExcepcionesNegocio.ReglaNegocioException("El salón ya tiene el plan PRO");
        }

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setCustomerEmail(emailUsuario)
                .setSuccessUrl(urlPublica + "/api/pagos/resultado?estado=exito")
                .setCancelUrl(urlPublica + "/api/pagos/resultado?estado=cancelado")
                .setClientReferenceId(String.valueOf(salonId))
                .putMetadata("salonId", String.valueOf(salonId))
                .putMetadata("tipoCompra", "SUSCRIPCION_PRO")
                .setSubscriptionData(SessionCreateParams.SubscriptionData.builder()
                        .putMetadata("salonId", String.valueOf(salonId))
                        .putMetadata("tipoCompra", "SUSCRIPCION_PRO")
                        .build())
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setPrice(precioPro)
                        .setQuantity(1L)
                        .build())
                .build();
        try {
            Session session = Session.create(params);
            return new SesionPagoRespuesta(TipoCompra.SUSCRIPCION_PRO, session.getId(), session.getUrl());
        } catch (StripeException ex) {
            logger.error("Error Stripe: {}", ex.getMessage(), ex);
            throw new ExcepcionesNegocio.PasarelaException("Error al crear la sesión de suscripción");
        }
    }

    public SesionPagoRespuesta crearSesionCreditos(Long salonId, String emailUsuario) {
        salonRepository.findById(salonId)
                .orElseThrow(() -> new ExcepcionesNegocio.RecursoNoEncontradoException("Salón no encontrado"));

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setClientReferenceId(String.valueOf(salonId))
                .putMetadata("salonId", String.valueOf(salonId))
                .putMetadata("tipoCompra", "PAQUETE_CREDITOS")
                .setSuccessUrl(urlPublica + "/api/pagos/resultado?estado=exito")
                .setCancelUrl(urlPublica + "/api/pagos/resultado?estado=cancelado")
                .setCustomerEmail(emailUsuario)
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setPrice(precioCreditos)
                        .setQuantity(1L)
                        .build())
                .build();
        try {
            Session session = Session.create(params);
            return new SesionPagoRespuesta(TipoCompra.PAQUETE_CREDITOS, session.getId(), session.getUrl());
        } catch (StripeException ex) {
            logger.error("Error Stripe: {}", ex.getMessage(), ex);
            throw new ExcepcionesNegocio.PasarelaException("Error al crear la sesión de créditos");
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

        Object objeto = event.getDataObjectDeserializer().getObject().orElse(null);

        if ("checkout.session.completed".equals(event.getType()) && objeto instanceof Session session) {
            String estadoPago = session.getPaymentStatus();
            if (estadoPago == null || "paid".equalsIgnoreCase(estadoPago)) {
                Map<String, String> metadata = session.getMetadata();
                String salonIdTexto = metadata == null ? null : metadata.get("salonId");
                if (salonIdTexto == null) {
                    salonIdTexto = session.getClientReferenceId();
                }

                String tipoCompraTexto = metadata == null ? null : metadata.get("tipoCompra");
                if (tipoCompraTexto == null) {
                    if ("subscription".equals(session.getMode())) {
                        tipoCompraTexto = "SUSCRIPCION_PRO";
                    } else if ("payment".equals(session.getMode())) {
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
                            salon.setStripeSuscripcionId(session.getSubscription());
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

        if ("customer.subscription.deleted".equals(event.getType()) && objeto instanceof Subscription subscription) {
            String subscriptionId = subscription.getId();
            salonRepository.findByStripeSuscripcionId(subscriptionId).ifPresent(salon -> {
                salon.setPlan(Plan.GRATIS);
                salon.setStripeSuscripcionId(null);
                salonRepository.save(salon);
            });
        }

        EventoStripe evento = new EventoStripe();
        evento.setId(event.getId());
        evento.setTipo(event.getType());
        evento.setFechaProcesado(LocalDateTime.now(clock));
        eventoStripeRepository.save(evento);
        return new WebhookRespuesta(true, true);
    }
}
