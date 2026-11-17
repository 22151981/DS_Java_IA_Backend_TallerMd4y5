package com.devsenior.sillon.catalogo;

import com.devsenior.sillon.seguridad.UsuarioAutenticado;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@Tag(name = "3 Servicios")
@RequestMapping("/api/servicios")
public class ServicioController {

    private final ServicioService servicioService;

    public ServicioController(ServicioService servicioService) {
        this.servicioService = servicioService;
    }

    @PostMapping
    @PreAuthorize("hasRole('DUENO')")
    public ResponseEntity<ServicioRespuesta> crearServicio(
            @AuthenticationPrincipal UsuarioAutenticado principal,
            @Valid @RequestBody CrearServicioRequest request) {
        ServicioRespuesta respuesta = servicioService.crearServicio(principal.salonId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @GetMapping
    public ResponseEntity<List<ServicioRespuesta>> listarServicios(
            @AuthenticationPrincipal UsuarioAutenticado principal) {
        List<ServicioRespuesta> lista = servicioService.listarPorSalon(principal.salonId());
        return ResponseEntity.ok(lista);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DUENO')")
    public ResponseEntity<ServicioRespuesta> actualizarServicio(
            @AuthenticationPrincipal UsuarioAutenticado principal,
            @PathVariable Long id,
            @Valid @RequestBody ActualizarServicioRequest request) {
        ServicioRespuesta respuesta = servicioService.actualizarServicio(principal.salonId(), id, request);
        return ResponseEntity.ok(respuesta);
    }

    @PatchMapping("/{id}/desactivar")
    @PreAuthorize("hasRole('DUENO')")
    public ResponseEntity<Void> desactivarServicio(
            @AuthenticationPrincipal UsuarioAutenticado principal,
            @PathVariable Long id) {
        servicioService.cambiarEstado(principal.salonId(), id, false);
        return ResponseEntity.noContent().build();
    }
}