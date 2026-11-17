package com.devsenior.sillon.chat;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ChatRequest(
        @NotBlank String clienteNombre,
        @NotBlank @Size(max = 120) String clienteContacto,
        @NotEmpty @Size(min = 1, max = 20) @Valid List<TurnoChatRequest> historial
) {}
