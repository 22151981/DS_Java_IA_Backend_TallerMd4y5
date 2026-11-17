package com.devsenior.sillon.salon;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

@RestController
@Tag(name = "9 Administración")
@RequestMapping("/api/admin/salones")
public class AdminController {
    private final SalonRepository salonRepository;

    public AdminController(SalonRepository salonRepository) {
        this.salonRepository = salonRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SalonRespuesta>> listar() {
        return ResponseEntity.ok(salonRepository.findAll().stream().map(SalonRespuesta::desdeEntidad).toList());
    }
}
