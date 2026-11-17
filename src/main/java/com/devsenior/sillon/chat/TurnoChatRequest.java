package com.devsenior.sillon.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TurnoChatRequest(
        @NotBlank String rol,
        @NotBlank @Size(max = 500) String texto
) {}
