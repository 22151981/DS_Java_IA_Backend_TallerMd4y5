package com.devsenior.sillon.publico;

import com.devsenior.sillon.cita.CitaRespuesta;
import com.devsenior.sillon.cita.CrearCitaRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDate;
import com.devsenior.sillon.chat.ChatRequest;
import com.devsenior.sillon.chat.ChatRespuesta;
import com.devsenior.sillon.chat.ChatService;

@RestController
@Tag(name = "0 Público")
@RequestMapping("/api/publico/salones")
public class PublicoController {

    private final PublicoService publicoService;
    private final ChatService chatService;

    public PublicoController(PublicoService publicoService, ChatService chatService) {
        this.publicoService = publicoService;
        this.chatService = chatService;
    }

    @GetMapping("/{slug}")
    public ResponseEntity<SalonPublicoRespuesta> obtenerSalon(@PathVariable String slug) {
        return ResponseEntity.ok(publicoService.obtenerSalonPublico(slug));
    }

    @GetMapping("/{slug}/disponibilidad")
    public ResponseEntity<DisponibilidadRespuesta> consultarDisponibilidad(
            @PathVariable String slug,
            @RequestParam Long servicioId,
            @RequestParam Long estilistaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(publicoService.consultarDisponibilidad(slug, servicioId, estilistaId, fecha));
    }

    @PostMapping("/{slug}/chat")
    public ResponseEntity<ChatRespuesta> chat(@PathVariable String slug, @Valid @RequestBody ChatRequest request) {
        return ResponseEntity.ok(chatService.conversar(slug, request));
    }

    @PostMapping("/{slug}/reservas")
    public ResponseEntity<CitaRespuesta> reservar(
            @PathVariable String slug,
            @Valid @RequestBody CrearCitaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(publicoService.reservarEnLinea(slug, request));
    }
}
