package com.devsenior.sillon.cita;

import com.devsenior.sillon.ia.FacturaIa;

public record RecordatorioRespuesta(Long citaId, String telefonoCliente, String mensaje, int creditosRestantes, FacturaIa factura) {}
