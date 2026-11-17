package com.devsenior.sillon.salon;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistroSalonRequest(
        @NotBlank(message = "El nombre del salón es obligatorio")
        @Size(max = 100)
        String salonNombre,

        @NotBlank(message = "La ciudad es obligatoria")
        @Size(max = 60)
        String ciudad,

        @NotBlank(message = "La dirección es obligatoria")
        @Size(max = 150)
        String direccion,

        @NotBlank(message = "El teléfono es obligatorio")
        @Size(max = 30)
        String telefono,

        @NotBlank(message = "El nombre del dueño es obligatorio")
        @Size(max = 100)
        String nombre,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Formato de email inválido")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
        String password
) {
}