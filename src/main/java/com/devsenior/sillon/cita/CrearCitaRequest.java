package com.devsenior.sillon.cita;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record CrearCitaRequest(
        @NotBlank String clienteNombre,
        @NotBlank @Size(min = 7, max = 30) String clienteTelefono,
        @NotNull Long servicioId,
        @NotNull Long estilistaId,
        @NotNull LocalDateTime inicio,
        @Size(max = 300) String nota
) {}
