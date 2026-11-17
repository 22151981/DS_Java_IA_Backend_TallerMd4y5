package com.devsenior.sillon.cita;

import com.devsenior.sillon.compartido.ExcepcionesNegocio;
import com.devsenior.sillon.salon.ConfiguracionSalon;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ReglasAgendaTest {
    private final ConfiguracionSalon config = new ConfiguracionSalon(LocalTime.of(8,0), LocalTime.of(19,0), false, 30);
    private final ReglasAgenda reglas = new ReglasAgenda();
    private final LocalDateTime ahora = LocalDateTime.of(2026,11,17,10,0);

    @Test void citaValidaDentroHorario() { assertDoesNotThrow(() -> reglas.validarHorario(LocalDateTime.of(2026,11,17,10,0), LocalDateTime.of(2026,11,17,11,0), config)); }
    @Test void terminaDespuesCierre() { assertThrows(ExcepcionesNegocio.ReglaNegocioException.class, () -> reglas.validarHorario(LocalDateTime.of(2026,11,17,18,30), LocalDateTime.of(2026,11,17,19,30), config)); }
    @Test void empiezaAntesApertura() { assertThrows(ExcepcionesNegocio.ReglaNegocioException.class, () -> reglas.validarHorario(LocalDateTime.of(2026,11,17,7,30), LocalDateTime.of(2026,11,17,8,30), config)); }
    @Test void domingoCuandoNoAbre() { assertThrows(ExcepcionesNegocio.ReglaNegocioException.class, () -> reglas.validarHorario(LocalDateTime.of(2026,11,22,10,0), LocalDateTime.of(2026,11,22,11,0), config)); }
    @Test void calculaLibresExcluyendoCruces() {
        Cita ocupada = new Cita(); ocupada.setInicio(LocalDateTime.of(2026,11,17,9,0)); ocupada.setFin(LocalDateTime.of(2026,11,17,10,0));
        List<LocalTime> libres = reglas.calcularHorariosLibres(LocalDate.of(2026,11,17), 60, config, List.of(ocupada), ahora);
        assertFalse(libres.contains(LocalTime.of(9,0))); assertFalse(libres.contains(LocalTime.of(8,30)));
    }
    @Test void diaCerradoDomingoListaVacia() { assertTrue(reglas.calcularHorariosLibres(LocalDate.of(2026,11,22), 60, config, List.of(), ahora).isEmpty()); }
    @Test void hoyExcluyeHorasPasadas() {
        List<LocalTime> libres = reglas.calcularHorariosLibres(LocalDate.of(2026,11,17), 60, config, List.of(), ahora);
        assertFalse(libres.contains(LocalTime.of(9,30))); assertTrue(libres.contains(LocalTime.of(10,30)));
    }
}
