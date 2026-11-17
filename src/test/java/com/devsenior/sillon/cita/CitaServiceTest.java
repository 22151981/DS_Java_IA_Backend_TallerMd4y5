package com.devsenior.sillon.cita;

import com.devsenior.sillon.catalogo.*;
import com.devsenior.sillon.cliente.*;
import com.devsenior.sillon.compartido.ExcepcionesNegocio;
import com.devsenior.sillon.ia.*;
import com.devsenior.sillon.salon.*;
import com.devsenior.sillon.usuario.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class CitaServiceTest {
    @Mock CitaRepository citas; @Mock ServicioRepository servicios; @Mock UsuarioRepository usuarios; @Mock ClienteRepository clientes;
    @Mock SalonRepository salones; @Mock ReglasAgenda reglas; @Mock AsistenteIaService ia; @Mock CreditoIaService creditos;
    @InjectMocks CitaService service;
    private final Clock clock=Clock.fixed(Instant.parse("2026-11-17T15:00:00Z"),ZoneId.of("America/Bogota"));

    private void setupBase(Salon salon,Servicio servicio,Usuario estilista) {
        when(servicios.findByIdAndSalonId(1L,1L)).thenReturn(Optional.of(servicio));
        when(usuarios.findByIdAndSalonId(2L,1L)).thenReturn(Optional.of(estilista));
        when(salones.findById(1L)).thenReturn(Optional.of(salon));
    }
    private Salon salon(){Salon s=new Salon();s.setId(1L);s.setConfiguracion(ConfiguracionSalon.porDefecto());return s;}
    private Servicio servicio(){Servicio s=new Servicio();s.setId(1L);s.setNombre("Corte");s.setDuracionMinutos(60);s.setPrecio(new BigDecimal("50000"));s.setActivo(true);return s;}
    private Usuario estilista(Servicio s){Usuario u=new Usuario();u.setId(2L);u.setNombre("Carlos");u.setRol(Rol.ESTILISTA);u.setActivo(true);u.setServicios(new HashSet<>(List.of(s)));return u;}
    private CrearCitaRequest req(LocalDateTime inicio){return new CrearCitaRequest("Ana","3000000000",1L,2L,inicio,"nota");}

    @Test void agendarValidaConfirmaCopiaPrecioYOrigenSalon() {
        Salon s=salon(); Servicio v=servicio(); Usuario u=estilista(v); setupBase(s,v,u);
        when(clientes.findBySalonId(1L)).thenReturn(List.of()); when(clientes.save(any())).thenAnswer(i->{Cliente c=i.getArgument(0);c.setNombre("Ana");return c;});
        when(citas.save(any())).thenAnswer(i->{Cita c=i.getArgument(0);c.setId(10L);return c;});
        CitaRespuesta r=new CitaService(citas,servicios,usuarios,clientes,salones,reglas,clock,ia,creditos).agendarCita(1L,req(LocalDateTime.of(2026,11,18,10,0)),OrigenCita.SALON,EstadoCita.CONFIRMADA);
        assertEquals(EstadoCita.CONFIRMADA,r.estado()); assertEquals(OrigenCita.SALON,r.origen()); assertEquals(new BigDecimal("50000"),r.precio());
    }
    @Test void pasadoLanza409SinGuardar() {
        Salon s=salon();Servicio v=servicio();Usuario u=estilista(v);setupBase(s,v,u);
        assertThrows(ExcepcionesNegocio.ReglaNegocioException.class,()->new CitaService(citas,servicios,usuarios,clientes,salones,reglas,clock,ia,creditos).agendarCita(1L,req(LocalDateTime.of(2026,11,16,10,0)),OrigenCita.SALON,EstadoCita.CONFIRMADA));
        verify(citas,never()).save(any());
    }
    @Test void cruceLanza409SinGuardar() {
        Salon s=salon();Servicio v=servicio();Usuario u=estilista(v);setupBase(s,v,u); when(citas.existeCruceDeHorario(1L,2L,LocalDateTime.of(2026,11,18,10,0),LocalDateTime.of(2026,11,18,11,0))).thenReturn(true);
        assertThrows(ExcepcionesNegocio.ReglaNegocioException.class,()->new CitaService(citas,servicios,usuarios,clientes,salones,reglas,clock,ia,creditos).agendarCita(1L,req(LocalDateTime.of(2026,11,18,10,0)),OrigenCita.SALON,EstadoCita.CONFIRMADA)); verify(citas,never()).save(any());
    }
    @Test void estilistaSinServicioLanza409() {
        Salon s=salon();Servicio v=servicio();Usuario u=estilista(new Servicio()); when(servicios.findByIdAndSalonId(1L,1L)).thenReturn(Optional.of(v)); when(usuarios.findByIdAndSalonId(2L,1L)).thenReturn(Optional.of(u));
        assertThrows(ExcepcionesNegocio.ReglaNegocioException.class,()->new CitaService(citas,servicios,usuarios,clientes,salones,reglas,clock,ia,creditos).agendarCita(1L,req(LocalDateTime.of(2026,11,18,10,0)),OrigenCita.SALON,EstadoCita.CONFIRMADA));
    }
    @Test void estilistaCambiarCitaAjena404() {
        Cita c=new Cita();Usuario otro=new Usuario();otro.setId(99L);c.setEstilista(otro);c.setEstado(EstadoCita.CONFIRMADA);when(citas.findByIdAndSalonId(5L,1L)).thenReturn(Optional.of(c));
        assertThrows(ExcepcionesNegocio.RecursoNoEncontradoException.class,()->new CitaService(citas,servicios,usuarios,clientes,salones,reglas,clock,ia,creditos).cambiarEstado(1L,2L,Rol.ESTILISTA,5L,EstadoCita.COMPLETADA));
    }
    @Test void estilistaCancelarPropia403() {
        Cita c=new Cita();Usuario u=new Usuario();u.setId(2L);c.setEstilista(u);c.setEstado(EstadoCita.CONFIRMADA);when(citas.findByIdAndSalonId(5L,1L)).thenReturn(Optional.of(c));
        assertThrows(ExcepcionesNegocio.AccionNoPermitidaException.class,()->new CitaService(citas,servicios,usuarios,clientes,salones,reglas,clock,ia,creditos).cambiarEstado(1L,2L,Rol.ESTILISTA,5L,EstadoCita.CANCELADA));
    }
    @Test void transicionNoPermitida409SinGuardar() {
        Cita c=new Cita();Usuario u=new Usuario();u.setId(2L);c.setEstilista(u);c.setEstado(EstadoCita.COMPLETADA);when(citas.findByIdAndSalonId(5L,1L)).thenReturn(Optional.of(c));
        assertThrows(ExcepcionesNegocio.ReglaNegocioException.class,()->new CitaService(citas,servicios,usuarios,clientes,salones,reglas,clock,ia,creditos).cambiarEstado(1L,2L,Rol.DUENO,5L,EstadoCita.CANCELADA)); verify(citas,never()).saveAndFlush(any());
    }
}
