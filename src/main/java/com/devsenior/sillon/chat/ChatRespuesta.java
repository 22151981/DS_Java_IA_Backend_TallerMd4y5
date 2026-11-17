package com.devsenior.sillon.chat;

import com.devsenior.sillon.ia.FacturaIa;

public record ChatRespuesta(String respuesta, boolean escalado, Long solicitudId, FacturaIa factura) {}
