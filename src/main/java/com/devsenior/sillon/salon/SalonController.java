package com.devsenior.sillon.salon;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/salones")
public class SalonController {

    private final SalonService salonService;
    private final com.devsenior.sillon.catalogo.ServicioService servicioService;
    private final com.devsenior.sillon.usuario.EstilistaService estilistaService;

    public SalonController(SalonService salonService,
                           com.devsenior.sillon.catalogo.ServicioService servicioService,
                           com.devsenior.sillon.usuario.EstilistaService estilistaService) {
        this.salonService = salonService;
        this.servicioService = servicioService;
        this.estilistaService = estilistaService;
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
    @GetMapping("/publico/{slug}/servicios")
    public ResponseEntity<java.util.List<com.devsenior.sillon.catalogo.ServicioRespuesta>> obtenerServiciosPublicos(
            @PathVariable String slug) {
        return ResponseEntity.ok(servicioService.listarActivosPorSlug(slug));
    }
    @GetMapping("/publico/{slug}/estilistas")
    public ResponseEntity<java.util.List<com.devsenior.sillon.usuario.EstilistaRespuesta>> obtenerEstilistasPublicos(
            @PathVariable String slug) {
        return ResponseEntity.ok(estilistaService.listarActivosPorSlug(slug));
    }

}