package com.devsenior.sillon.catalogo;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ActualizarServicioRequest(
        @NotBlank(message = "El nombre del servicio es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String nombre,

        @Size(max = 250, message = "La descripción no puede superar 250 caracteres")
        String descripcion,

        @NotNull(message = "La duración en minutos es obligatoria")
        @Min(value = 15, message = "La duración mínima es 15 minutos")
        @Max(value = 480, message = "La duración máxima es 480 minutos")
        Integer duracionMinutos,

        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
        BigDecimal precio,

        @NotNull(message = "El estado activo es obligatorio")
        Boolean activo
) {
}