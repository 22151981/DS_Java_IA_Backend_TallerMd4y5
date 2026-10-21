package com.devsenior.sillon.salon;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/salones")
public class SalonController {

    private final SalonService salonService;

    public SalonController(SalonService salonService) {
        this.salonService = salonService;
    }

    @PostMapping("/registro")
    public ResponseEntity<SalonRespuesta> registrarSalon(@Valid @RequestBody RegistroSalonRequest request) {
        SalonRespuesta respuesta = salonService.registrarSalon(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @GetMapping("/publico/{slug}")
    public ResponseEntity<SalonRespuesta> obtenerSalonPublico(@PathVariable String slug) {
        SalonRespuesta respuesta = salonService.obtenerPorSlug(slug);
        return ResponseEntity.ok(respuesta);
    }
    @GetMapping("/mi-salon/configuracion")
    public ResponseEntity<ConfiguracionSalon> obtenerMiConfiguracion(
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.devsenior.sillon.seguridad.UsuarioAutenticado principal) {
        ConfiguracionSalon config = salonService.obtenerConfiguracionPorId(principal.salonId());
        return ResponseEntity.ok(config);
    }

    @PutMapping("/mi-salon/configuracion")
    public ResponseEntity<ConfiguracionSalon> actualizarConfiguracion(
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.devsenior.sillon.seguridad.UsuarioAutenticado principal,
            @Valid @RequestBody ConfiguracionSalonRequest request) {
        ConfiguracionSalon config = salonService.actualizarConfiguracion(principal.salonId(), request);
        return ResponseEntity.ok(config);
    }
}