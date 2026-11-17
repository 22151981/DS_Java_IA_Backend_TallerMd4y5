package com.devsenior.sillon.cita;

import com.devsenior.sillon.seguridad.UsuarioAutenticado;
import com.devsenior.sillon.usuario.Rol;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/citas")
public class CitaController {
    private final CitaService citaService;

    public CitaController(CitaService citaService) { this.citaService = citaService; }

    @PostMapping
    @PreAuthorize("hasRole('DUENO')")
    public ResponseEntity<CitaRespuesta> crear(@AuthenticationPrincipal UsuarioAutenticado principal,
                                               @Valid @RequestBody CrearCitaRequest request) {
        CitaRespuesta respuesta = citaService.agendarCita(principal.salonId(), request,
                OrigenCita.SALON, EstadoCita.CONFIRMADA);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DUENO','ESTILISTA')")
    public ResponseEntity<List<CitaRespuesta>> listar(@AuthenticationPrincipal UsuarioAutenticado principal,
                                                       @RequestParam(required = false)
                                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        Rol rol = Rol.valueOf(principal.rol());
        return ResponseEntity.ok(citaService.listarCitasPorDia(principal.salonId(), principal.usuarioId(), rol, fecha));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('DUENO','ESTILISTA')")
    public ResponseEntity<CitaRespuesta> cambiarEstado(@AuthenticationPrincipal UsuarioAutenticado principal,
                                                        @PathVariable Long id,
                                                        @Valid @RequestBody CambiarEstadoCitaRequest request) {
        Rol rol = Rol.valueOf(principal.rol());
        CitaRespuesta respuesta = citaService.cambiarEstado(principal.salonId(), principal.usuarioId(), rol, id, request.estado());
        return ResponseEntity.ok(respuesta);
    }
}
