package com.devsenior.sillon.cita;

import com.devsenior.sillon.compartido.ExcepcionesNegocio;
import com.devsenior.sillon.salon.ConfiguracionSalon;
import org.springframework.stereotype.Component;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class ReglasAgenda {
    public void validarHorario(LocalDateTime inicio, LocalDateTime fin, ConfiguracionSalon config) {
        if (!Boolean.TRUE.equals(config.getAbreDomingos()) && inicio.getDayOfWeek() == DayOfWeek.SUNDAY) {
            throw new ExcepcionesNegocio.ReglaNegocioException("El salón no atiende los domingos. Horario: " + config.getHoraApertura() + " a " + config.getHoraCierre());
        }
        if (!inicio.toLocalDate().equals(fin.toLocalDate())
                || inicio.toLocalTime().isBefore(config.getHoraApertura())
                || fin.toLocalTime().isAfter(config.getHoraCierre())) {
            throw new ExcepcionesNegocio.ReglaNegocioException("La cita debe estar dentro del horario del salón: " + config.getHoraApertura() + " a " + config.getHoraCierre());
        }
    }

    public List<LocalTime> calcularHorariosLibres(LocalDate fecha, int duracionMinutos, ConfiguracionSalon config,
                                                   List<Cita> citasOcupadas, LocalDateTime ahora) {
        List<LocalTime> libres = new ArrayList<>();
        if (fecha.getDayOfWeek() == DayOfWeek.SUNDAY && !Boolean.TRUE.equals(config.getAbreDomingos())) return libres;
        LocalTime slot = config.getHoraApertura();
        int intervalo = config.getIntervaloMinutos() == null ? 30 : config.getIntervaloMinutos();
        while (!slot.plusMinutes(duracionMinutos).isAfter(config.getHoraCierre())) {
            LocalDateTime slotInicio = LocalDateTime.of(fecha, slot);
            LocalDateTime slotFin = slotInicio.plusMinutes(duracionMinutos);
            boolean disponible = !slotInicio.isBefore(ahora) && !slotInicio.isEqual(ahora);
            if (disponible) {
                for (Cita cita : citasOcupadas) {
                    if (slotInicio.isBefore(cita.getFin()) && slotFin.isAfter(cita.getInicio())) {
                        disponible = false;
                        break;
                    }
                }
            }
            if (disponible) libres.add(slot);
            slot = slot.plusMinutes(intervalo);
        }
        return libres;
    }
}
