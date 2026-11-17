package com.devsenior.sillon.pago;

import com.devsenior.sillon.seguridad.UsuarioAutenticado;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;

@RestController
@Tag(name = "7 Pagos")
@RequestMapping("/api/pagos")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @PostMapping("/suscripcion")
    @PreAuthorize("hasRole('DUENO')")
    public ResponseEntity<SesionPagoRespuesta> crearSuscripcion(
            @AuthenticationPrincipal UsuarioAutenticado principal) {
        return ResponseEntity.ok(pagoService.crearSesionSuscripcion(principal.salonId(), principal.email()));
    }

    @PostMapping("/creditos")
    @PreAuthorize("hasRole('DUENO')")
    public ResponseEntity<SesionPagoRespuesta> crearCreditos(
            @AuthenticationPrincipal UsuarioAutenticado principal) {
        return ResponseEntity.ok(pagoService.crearSesionCreditos(principal.salonId(), principal.email()));
    }

    @PostMapping("/webhook")
    public ResponseEntity<WebhookRespuesta> webhook(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader) {
        return ResponseEntity.ok(pagoService.procesarWebhook(payload, sigHeader));
    }

    @GetMapping("/resultado")
    public ResponseEntity<Map<String, String>> resultado(
            @RequestParam(defaultValue = "exito") String estado) {
        if ("cancelado".equalsIgnoreCase(estado)) {
            return ResponseEntity.ok(Map.of("estado", "cancelado", "mensaje", "Pago cancelado"));
        }
        return ResponseEntity.ok(Map.of("estado", "exito", "mensaje", "Pago completado"));
    }
}
