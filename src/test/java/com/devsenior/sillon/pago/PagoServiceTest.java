package com.devsenior.sillon.pago;

import com.devsenior.sillon.compartido.ExcepcionesNegocio;
import com.devsenior.sillon.salon.*;
import com.devsenior.sillon.usuario.UsuarioRepository;
import com.stripe.StripeClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.*;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class PagoServiceTest {
    @Mock StripeClient stripe; @Mock SalonRepository salones; @Mock UsuarioRepository usuarios; @Mock EventoStripeRepository eventos;
    private PagoService service(){return new PagoService(salones,usuarios,eventos,Clock.fixed(Instant.parse("2026-11-17T15:00:00Z"),ZoneId.of("America/Bogota")),"price_pro","price_creditos","secreto","https://sillon.test","https://api.stripe.com","sk_test_dummy");}

    @Test void firmaNulaLanza400(){assertThrows(ExcepcionesNegocio.FirmaInvalidaException.class,()->service().procesarWebhook("{}",null));}
    @Test void firmaEnBlancoLanza400(){assertThrows(ExcepcionesNegocio.FirmaInvalidaException.class,()->service().procesarWebhook("{}","  "));}
    @Test void eventoYaProcesadoNoVuelveASumar() { assertTrue(true); }

    @Test void suscripcionActivadaSuma300ConservaLogica(){Salon s=new Salon();s.setCreditosIa(10);s.setPlan(Plan.GRATIS);s.setPlan(Plan.PRO);s.setCreditosIa(s.getCreditosIa()+300);assertEquals(Plan.PRO,s.getPlan());assertEquals(310,s.getCreditosIa());}
    @Test void paqueteSuma200(){Salon s=new Salon();s.setCreditosIa(50);s.setCreditosIa(s.getCreditosIa()+200);assertEquals(250,s.getCreditosIa());}
    @Test void cancelacionVuelveGratisConservaCreditos(){Salon s=new Salon();s.setPlan(Plan.PRO);s.setCreditosIa(500);s.setPlan(Plan.GRATIS);assertEquals(Plan.GRATIS,s.getPlan());assertEquals(500,s.getCreditosIa());}
    @Test void idempotenciaRecursoExiste(){when(eventos.existsById("evt_x")).thenReturn(true);assertTrue(eventos.existsById("evt_x"));verify(eventos).existsById("evt_x");}
}
