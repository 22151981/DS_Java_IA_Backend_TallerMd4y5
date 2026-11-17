package com.devsenior.sillon.usuario;

import jakarta.validation.constraints.*;
import java.util.List;

public record CrearEstilistaRequest(
        @NotBlank String nombre,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotEmpty(message = "Debe asignar al menos un servicio") List<Long> servicioIds
) {
}