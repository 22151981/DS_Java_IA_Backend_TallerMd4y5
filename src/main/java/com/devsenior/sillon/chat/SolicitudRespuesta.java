package com.devsenior.sillon.chat;

import java.time.LocalDateTime;

public record SolicitudRespuesta(Long id, String clienteNombre, String clienteContacto, String mensajes,
                                 MotivoSolicitud motivo, EstadoSolicitud estado, LocalDateTime fechaCreacion) {}
