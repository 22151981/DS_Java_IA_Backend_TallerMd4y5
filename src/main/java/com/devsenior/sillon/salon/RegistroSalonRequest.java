package com.devsenior.sillon.salon;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistroSalonRequest(
        @NotBlank(message = "El nombre del salón es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String nombreSalon,

        @NotBlank(message = "La ciudad es obligatoria")
        @Size(max = 60, message = "La ciudad no puede superar 60 caracteres")
        String ciudad,

        @NotBlank(message = "La dirección es obligatoria")
        @Size(max = 150, message = "La dirección no puede superar 150 caracteres")
        String direccion,

        @NotBlank(message = "El teléfono es obligatorio")
        @Size(max = 30, message = "El teléfono no puede superar 30 caracteres")
        String telefono,

        @NotBlank(message = "El nombre del dueño es obligatorio")
        @Size(max = 100, message = "El nombre del dueño no puede superar 100 caracteres")
        String nombreDueno,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Formato de correo inválido")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, message = "La contraseña debe tener mínimo 6 caracteres")
        String password
) {
}