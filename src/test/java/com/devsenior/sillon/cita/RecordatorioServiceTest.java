package com.devsenior.sillon.cita;

import com.devsenior.sillon.catalogo.*;
import com.devsenior.sillon.cliente.*;
import com.devsenior.sillon.compartido.ExcepcionesNegocio;
import com.devsenior.sillon.ia.*;
import com.devsenior.sillon.salon.*;
import com.devsenior.sillon.usuario.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecordatorioServiceTest {
    @Mock CitaRepository citas; @Mock ServicioRepository servicios; @Mock UsuarioRepository usuarios;
    @Mock ClienteRepository clientes; @Mock SalonRepository salones; @Mock CreditoIaService creditos;
    @Mock AsistenteIaService ia;

    private CitaService service() { return new CitaService(citas,servicios,usuarios,clientes,salones,new ReglasAgenda(),Clock.fixed(Instant.parse("2026-11-17T15:00:00Z"),ZoneId.of("America/Bogota")),ia,creditos); }
    private Cita cita() { Cita c=new Cita();c.setId(5L);c.setEstado(EstadoCita.CONFIRMADA);c.setInicio(LocalDateTime.of(2026,11,18,10,0));c.setPrecio(new BigDecimal("50000"));
        Servicio s=new Servicio();s.setNombre("Corte");s.setDuracionMinutos(60);c.setServicio(s); Usuario u=new Usuario();u.setNombre("Carlos");c.setEstilista(u); Cliente x=new Cliente();x.setNombre("Ana");x.setTelefono("3001");c.setCliente(x);
        Salon salon=new Salon();salon.setNombre("Sillón");salon.setDireccion("Centro");salon.setCiudad("Bogotá");c.setSalon(salon);return c; }

    @Test void respuestaValidaEntregaMensajeYConsumeCredito() {
        Cita c=cita(); when(citas.findByIdAndSalonId(5L,1L)).thenReturn(Optional.of(c)); when(ia.generar(anyString(),anyString())).thenReturn(new RespuestaIa("{\"mensaje\":\"Recuerda tu cita\"}",null));
        when(creditos.consumirCredito(1L)).thenReturn(9);
        RecordatorioRespuesta r=service().generarRecordatorio(1L,5L); assertEquals("Recuerda tu cita",r.mensaje()); assertEquals(9,r.creditosRestantes()); verify(creditos).consumirCredito(1L);
    }
    @Test void jsonSinMensajeLanza502() {
        Cita c=cita(); when(citas.findByIdAndSalonId(5L,1L)).thenReturn(Optional.of(c)); when(ia.generar(anyString(),anyString())).thenReturn(new RespuestaIa("{\"texto\":\"sin clave\"}",null));
        assertThrows(ExcepcionesNegocio.PasarelaIaInvalidaException.class,()->service().generarRecordatorio(1L,5L));
    }
    @Test void sinCreditosLanza402SinIA() {
        Cita c=cita(); when(citas.findByIdAndSalonId(5L,1L)).thenReturn(Optional.of(c)); doThrow(new ExcepcionesNegocio.PagoRequeridoException("sin créditos")).when(creditos).verificarDisponibilidad(1L);
        assertThrows(ExcepcionesNegocio.PagoRequeridoException.class,()->service().generarRecordatorio(1L,5L)); verify(ia,never()).generar(anyString(),anyString());
    }
}
