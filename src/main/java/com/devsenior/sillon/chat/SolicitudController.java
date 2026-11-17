package com.devsenior.sillon.chat;

import com.devsenior.sillon.compartido.ExcepcionesNegocio;
import com.devsenior.sillon.seguridad.UsuarioAutenticado;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

@RestController
@Tag(name = "8 Solicitudes")
@RequestMapping("/api/solicitudes")
public class SolicitudController {
    private final SolicitudContactoRepository repository;

    public SolicitudController(SolicitudContactoRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @PreAuthorize("hasRole('DUENO')")
    public ResponseEntity<List<SolicitudRespuesta>> listar(@AuthenticationPrincipal UsuarioAutenticado principal) {
        return ResponseEntity.ok(repository.findBySalonIdOrderByFechaCreacionDesc(principal.salonId()).stream()
                .map(SolicitudContacto::aRespuesta).toList());
    }

    @PatchMapping("/{id}/atendida")
    @PreAuthorize("hasRole('DUENO')")
    public ResponseEntity<SolicitudRespuesta> atender(@AuthenticationPrincipal UsuarioAutenticado principal,
                                                     @PathVariable Long id) {
        SolicitudContacto solicitud = repository.findByIdAndSalonId(id, principal.salonId())
                .orElseThrow(() -> new ExcepcionesNegocio.RecursoNoEncontradoException("Solicitud no encontrada en este salón"));
        if (solicitud.getEstado() == EstadoSolicitud.ATENDIDA) {
            throw new ExcepcionesNegocio.ReglaNegocioException("La solicitud ya está atendida");
        }
        solicitud.setEstado(EstadoSolicitud.ATENDIDA);
        return ResponseEntity.ok(repository.save(solicitud).aRespuesta());
    }
}
