package com.devsenior.sillon.chat;

import com.devsenior.sillon.catalogo.Servicio;
import com.devsenior.sillon.catalogo.ServicioRepository;
import com.devsenior.sillon.compartido.ExcepcionesNegocio;
import com.devsenior.sillon.ia.AsistenteIaService;
import com.devsenior.sillon.ia.CreditoIaService;
import com.devsenior.sillon.ia.RespuestaIa;
import com.devsenior.sillon.salon.Salon;
import com.devsenior.sillon.salon.SalonRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatService {
    private final SalonRepository salonRepository;
    private final ServicioRepository servicioRepository;
    private final SolicitudContactoRepository solicitudRepository;
    private final CreditoIaService creditoIaService;
    private final AsistenteIaService asistenteIaService;
    private final Clock clock;

    public ChatService(SalonRepository salonRepository, ServicioRepository servicioRepository,
                       SolicitudContactoRepository solicitudRepository, CreditoIaService creditoIaService,
                       AsistenteIaService asistenteIaService, Clock clock) {
        this.salonRepository = salonRepository;
        this.servicioRepository = servicioRepository;
        this.solicitudRepository = solicitudRepository;
        this.creditoIaService = creditoIaService;
        this.asistenteIaService = asistenteIaService;
        this.clock = clock;
    }

    @Transactional
    public ChatRespuesta conversar(String slug, ChatRequest request) {
        Salon salon = salonRepository.findBySlug(slug)
                .orElseThrow(() -> new ExcepcionesNegocio.RecursoNoEncontradoException("Salón no encontrado"));
        creditoIaService.verificarDisponibilidad(salon.getId());

        List<Servicio> activos = servicioRepository.findBySalonIdAndActivoTrue(salon.getId());
        StringBuilder catalogo = new StringBuilder();
        for (Servicio servicio : activos) {
            catalogo.append(servicio.getNombre()).append(" (duración ").append(servicio.getDuracionMinutos())
                    .append(" minutos, precio ").append(servicio.getPrecio().intValueExact()).append("); ");
        }
        String horario = salon.getConfiguracion() == null ? "No especificado" :
                salon.getConfiguracion().getHoraApertura() + " a " + salon.getConfiguracion().getHoraCierre()
                        + (Boolean.TRUE.equals(salon.getConfiguracion().getAbreDomingos()) ? ", abre domingos" : ", no abre domingos");
        StringBuilder historial = new StringBuilder();
        for (TurnoChatRequest turno : request.historial()) {
            String etiqueta = "ASISTENTE".equalsIgnoreCase(turno.rol()) ? "Asistente" : "Cliente";
            historial.append(etiqueta).append(": ").append(turno.texto()).append("\n");
        }

        String prompt;
        try {
            prompt = new String(new ClassPathResource("prompts/chatbot.txt").getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception ex) { throw new RuntimeException("No se pudo cargar el prompt", ex); }
        prompt = prompt.replace("{salon}", salon.getNombre()).replace("{direccion}", salon.getDireccion() + ", " + salon.getCiudad())
                .replace("{horario}", horario).replace("{catalogo}", catalogo.toString()).replace("{cliente}", request.clienteNombre())
                + "\nHistorial:\n" + historial;

        RespuestaIa ia = asistenteIaService.generar("Eres un asistente virtual que devuelve JSON estricto.", prompt);
        int restantes = creditoIaService.consumirCredito(salon.getId());

        String respuesta;
        boolean escalado;
        try {
            JsonNode raiz = new ObjectMapper().readTree(ia.contenidoJson());
            JsonNode nodoRespuesta = raiz.path("respuesta");
            if (!nodoRespuesta.isTextual() || nodoRespuesta.asText().isBlank()) {
                throw new ExcepcionesNegocio.PasarelaIaInvalidaException("La respuesta de IA no tiene los campos requeridos");
            }
            respuesta = nodoRespuesta.asText();
            escalado = raiz.path("escalar").asBoolean(false);
        } catch (ExcepcionesNegocio.PasarelaIaInvalidaException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ExcepcionesNegocio.PasarelaIaInvalidaException("Respuesta JSON inválida del proveedor de IA");
        }

        String minuscula = respuesta.toLowerCase();
        String[] prohibidas = {"descuento", "cupón", "cupon", "gratis", "promoción", "promocion", "reembols"};
        Long solicitudId = null;
        boolean seguridad = false;
        for (String palabra : prohibidas) if (minuscula.contains(palabra)) seguridad = true;
        if (seguridad) {
            respuesta = "Con gusto un miembro de nuestro equipo te atenderá personalmente para darte información detallada.";
            escalado = true;
        }
        if (escalado) {
            SolicitudContacto solicitud = new SolicitudContacto();
            solicitud.setClienteNombre(request.clienteNombre());
            solicitud.setClienteContacto(request.clienteContacto());
            solicitud.setMensajes(truncar(historial.toString(), 2000));
            solicitud.setMotivo(seguridad ? MotivoSolicitud.REGLA_DE_SEGURIDAD : MotivoSolicitud.PEDIDO_POR_IA);
            solicitud.setEstado(EstadoSolicitud.PENDIENTE);
            solicitud.setFechaCreacion(LocalDateTime.now(clock));
            solicitud.setSalon(salon);
            solicitudId = solicitudRepository.save(solicitud).getId();
        }
        return new ChatRespuesta(respuesta, escalado, solicitudId, ia.factura());
    }

    private String truncar(String texto, int maximo) {
        return texto.length() <= maximo ? texto : texto.substring(0, maximo);
    }
}
