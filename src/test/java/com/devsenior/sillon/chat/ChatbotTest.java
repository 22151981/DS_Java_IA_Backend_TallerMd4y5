package com.devsenior.sillon.chat;

import com.devsenior.sillon.catalogo.*;
import com.devsenior.sillon.compartido.ExcepcionesNegocio;
import com.devsenior.sillon.ia.*;
import com.devsenior.sillon.salon.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatbotTest {
    @Mock SalonRepository salones; @Mock ServicioRepository servicios; @Mock SolicitudContactoRepository solicitudes;
    @Mock CreditoIaService creditos; @Mock AsistenteIaService ia;

    private ChatService servicio() { return new ChatService(salones,servicios,solicitudes,creditos,ia, Clock.fixed(Instant.parse("2026-11-17T15:00:00Z"), ZoneId.of("America/Bogota"))); }
    private Salon salon() { Salon s=new Salon(); s.setId(1L); s.setNombre("Sillón"); s.setCiudad("Bogotá"); s.setDireccion("Centro"); s.setConfiguracion(ConfiguracionSalon.porDefecto()); return s; }
    private ChatRequest request() { return new ChatRequest("Ana","3000000000",List.of(new TurnoChatRequest("usuario","Hola"))); }

    @Test void normalNoEscalaNiSolicitud() {
        when(salones.findBySlug("salon")).thenReturn(Optional.of(salon())); when(servicios.findBySalonIdAndActivoTrue(1L)).thenReturn(List.of());
        when(ia.generar(anyString(),anyString())).thenReturn(new RespuestaIa("{\"respuesta\":\"Hola\",\"escalar\":false}",null)); when(creditos.consumirCredito(1L)).thenReturn(9);
        ChatRespuesta r=servicio().conversar("salon",request()); assertFalse(r.escalado()); assertNull(r.solicitudId()); verify(creditos).consumirCredito(1L);
    }
    @Test void iaIndicaEscalarGuardaSolicitud() {
        when(salones.findBySlug("salon")).thenReturn(Optional.of(salon())); when(servicios.findBySalonIdAndActivoTrue(1L)).thenReturn(List.of());
        when(ia.generar(anyString(),anyString())).thenReturn(new RespuestaIa("{\"respuesta\":\"Agendo\",\"escalar\":true}",null));
        when(creditos.consumirCredito(1L)).thenReturn(8); when(solicitudes.save(any())).thenAnswer(i->{ SolicitudContacto s=i.getArgument(0); s.setId(42L); return s; });
        ChatRespuesta r=servicio().conversar("salon",request()); assertTrue(r.escalado()); assertEquals(42L,r.solicitudId());
        ArgumentCaptor<SolicitudContacto> c=ArgumentCaptor.forClass(SolicitudContacto.class); verify(solicitudes).save(c.capture()); assertEquals(MotivoSolicitud.PEDIDO_POR_IA,c.getValue().getMotivo());
    }
    @Test void promesaDescuentoSeSustituyeYEscalaPorSeguridad() {
        when(salones.findBySlug("salon")).thenReturn(Optional.of(salon()));
        when(servicios.findBySalonIdAndActivoTrue(1L)).thenReturn(List.of());
        when(ia.generar(anyString(),anyString())).thenReturn(new RespuestaIa("{\"respuesta\":\"Usa GRATIS con cupón SILLA50\",\"escalar\":false}",null));
        when(creditos.consumirCredito(1L)).thenReturn(7);
        when(solicitudes.save(any())).thenAnswer(i->{SolicitudContacto s=i.getArgument(0);s.setId(1L);return s;});
        ChatRespuesta r=servicio().conversar("salon",request());
        assertTrue(r.escalado()); assertTrue(r.respuesta().contains("miembro"));
        ArgumentCaptor<SolicitudContacto> c=ArgumentCaptor.forClass(SolicitudContacto.class);
        verify(solicitudes).save(c.capture()); assertEquals(MotivoSolicitud.REGLA_DE_SEGURIDAD,c.getValue().getMotivo());
    }
}
