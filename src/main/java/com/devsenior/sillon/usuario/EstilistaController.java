package com.devsenior.sillon.usuario;

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
@Tag(name = "4 Estilistas")
@RequestMapping("/api/estilistas")
public class EstilistaController {

    private final EstilistaService estilistaService;

    public EstilistaController(EstilistaService estilistaService) {
        this.estilistaService = estilistaService;
    }

    @PostMapping
    @PreAuthorize("hasRole('DUENO')")
    public ResponseEntity<EstilistaRespuesta> crearEstilista(
            @AuthenticationPrincipal UsuarioAutenticado principal,
            @Valid @RequestBody CrearEstilistaRequest request) {
        EstilistaRespuesta respuesta = estilistaService.crearEstilista(principal.salonId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @GetMapping
    public ResponseEntity<List<EstilistaRespuesta>> listarEstilistas(
            @AuthenticationPrincipal UsuarioAutenticado principal) {
        List<EstilistaRespuesta> lista = estilistaService.listarPorSalon(principal.salonId());
        return ResponseEntity.ok(lista);
    }

    @PutMapping("/{id}/servicios")
    @PreAuthorize("hasRole('DUENO')")
    public ResponseEntity<EstilistaRespuesta> asignarServicios(
            @AuthenticationPrincipal UsuarioAutenticado principal,
            @PathVariable Long id,
            @Valid @RequestBody AsignarServiciosRequest request) {
        EstilistaRespuesta respuesta = estilistaService.asignarServicios(principal.salonId(), id, request);
        return ResponseEntity.ok(respuesta);
    }

    @PatchMapping("/{id}/desactivar")
    @PreAuthorize("hasRole('DUENO')")
    public ResponseEntity<Void> desactivarEstilista(
            @AuthenticationPrincipal UsuarioAutenticado principal,
            @PathVariable Long id) {
        estilistaService.cambiarEstado(principal.salonId(), id, false);
        return ResponseEntity.noContent().build();
    }
}