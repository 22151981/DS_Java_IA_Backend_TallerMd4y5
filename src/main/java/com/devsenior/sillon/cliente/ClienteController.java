package com.devsenior.sillon.cliente;

import com.devsenior.sillon.seguridad.UsuarioAutenticado;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@Tag(name = "5 Clientes")
@RequestMapping("/api/clientes")
public class ClienteController {
    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    @PreAuthorize("hasRole('DUENO')")
    public ResponseEntity<List<ClienteRespuesta>> listar(@AuthenticationPrincipal UsuarioAutenticado principal) {
        return ResponseEntity.ok(clienteService.listarPorSalon(principal.salonId()));
    }
}
