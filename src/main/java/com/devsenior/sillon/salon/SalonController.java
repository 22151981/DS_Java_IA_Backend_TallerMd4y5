package com.devsenior.sillon.salon;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "2 Mi salón")
@RequestMapping({"/api/salones", "/api/salon"})
public class SalonController {

    private final SalonService salonService;

    public SalonController(SalonService salonService) {
        this.salonService = salonService;
    }

    @GetMapping({"/mi-salon/configuracion", ""})
    public ResponseEntity<ConfiguracionSalon> obtenerMiConfiguracion(
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.devsenior.sillon.seguridad.UsuarioAutenticado principal) {
        if (principal == null || principal.salonId() == null) {
            throw new com.devsenior.sillon.compartido.ExcepcionesNegocio.CredencialesInvalidasException("Usuario no autenticado");
        }
        ConfiguracionSalon config = salonService.obtenerConfiguracionPorId(principal.salonId());
        return ResponseEntity.ok(config);
    }

    @PutMapping("/mi-salon/configuracion")
    public ResponseEntity<ConfiguracionSalon> actualizarConfiguracion(
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.devsenior.sillon.seguridad.UsuarioAutenticado principal,
            @Valid @RequestBody ConfiguracionSalonRequest request) {
        if (principal == null || principal.salonId() == null) {
            throw new com.devsenior.sillon.compartido.ExcepcionesNegocio.CredencialesInvalidasException("Usuario no autenticado");
        }
        ConfiguracionSalon config = salonService.actualizarConfiguracion(principal.salonId(), request);
        return ResponseEntity.ok(config);
    }

}