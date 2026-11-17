package com.devsenior.sillon.publico;

import com.devsenior.sillon.catalogo.Servicio;
import com.devsenior.sillon.catalogo.ServicioRepository;
import com.devsenior.sillon.cita.Cita;
import com.devsenior.sillon.cita.CitaRepository;
import com.devsenior.sillon.cita.CitaRespuesta;
import com.devsenior.sillon.cita.CitaService;
import com.devsenior.sillon.cita.CrearCitaRequest;
import com.devsenior.sillon.cita.EstadoCita;
import com.devsenior.sillon.cita.OrigenCita;
import com.devsenior.sillon.cita.ReglasAgenda;
import com.devsenior.sillon.compartido.ExcepcionesNegocio;
import com.devsenior.sillon.salon.ConfiguracionSalon;
import com.devsenior.sillon.salon.Salon;
import com.devsenior.sillon.salon.SalonRepository;
import com.devsenior.sillon.usuario.Rol;
import com.devsenior.sillon.usuario.Usuario;
import com.devsenior.sillon.usuario.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class PublicoService {

    private final SalonRepository salonRepository;
    private final ServicioRepository servicioRepository;
    private final UsuarioRepository usuarioRepository;
    private final CitaRepository citaRepository;
    private final CitaService citaService;
    private final ReglasAgenda reglasAgenda;
    private final Clock clock;

    public PublicoService(SalonRepository salonRepository,
                          ServicioRepository servicioRepository,
                          UsuarioRepository usuarioRepository,
                          CitaRepository citaRepository,
                          CitaService citaService,
                          ReglasAgenda reglasAgenda,
                          Clock clock) {
        this.salonRepository = salonRepository;
        this.servicioRepository = servicioRepository;
        this.usuarioRepository = usuarioRepository;
        this.citaRepository = citaRepository;
        this.citaService = citaService;
        this.reglasAgenda = reglasAgenda;
        this.clock = clock;
    }

    public SalonPublicoRespuesta obtenerSalonPublico(String slug) {
        Salon salon = buscarSalon(slug);
        ConfiguracionSalon config = salon.getConfiguracion();

        List<ServicioPublicoRespuesta> servicios = servicioRepository
                .findBySalonIdAndActivoTrue(salon.getId()).stream()
                .map(ServicioPublicoRespuesta::desdeEntidad)
                .toList();

        List<EstilistaPublicoRespuesta> estilistas = usuarioRepository
                .findBySalonIdAndRolAndActivoTrue(salon.getId(), Rol.ESTILISTA).stream()
                .map(EstilistaPublicoRespuesta::desdeEntidad)
                .toList();

        return new SalonPublicoRespuesta(
                salon.getNombre(), salon.getSlug(), salon.getCiudad(), salon.getDireccion(),
                salon.getTelefono(), describirHorario(config), servicios, estilistas);
    }

    @Transactional(readOnly = true)
    public DisponibilidadRespuesta consultarDisponibilidad(String slug, Long servicioId,
                                                            Long estilistaId, LocalDate fecha) {
        Salon salon = buscarSalon(slug);
        Servicio servicio = servicioRepository.findByIdAndSalonId(servicioId, salon.getId())
                .filter(Servicio::getActivo)
                .orElseThrow(() -> new ExcepcionesNegocio.RecursoNoEncontradoException("Servicio no encontrado en este salón"));

        Usuario estilista = usuarioRepository.findByIdAndSalonId(estilistaId, salon.getId())
                .filter(u -> u.getRol() == Rol.ESTILISTA && Boolean.TRUE.equals(u.getActivo()))
                .orElseThrow(() -> new ExcepcionesNegocio.RecursoNoEncontradoException("Estilista no encontrado en este salón"));

        if (!estilista.getServicios().contains(servicio)) {
            throw new ExcepcionesNegocio.ReglaNegocioException("El estilista no ofrece este servicio");
        }

        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin = fecha.plusDays(1).atStartOfDay();
        List<Cita> citasOcupadas = citaRepository.listarOcupadasPorEstilistaDia(
                salon.getId(), estilista.getId(), inicio, fin);
        List<LocalTime> horariosLibres = reglasAgenda.calcularHorariosLibres(
                fecha, servicio.getDuracionMinutos(), salon.getConfiguracion(),
                citasOcupadas, LocalDateTime.now(clock));

        return new DisponibilidadRespuesta(fecha, servicio.getNombre(), estilista.getNombre(),
                servicio.getDuracionMinutos(), horariosLibres);
    }

    @Transactional
    public CitaRespuesta reservarEnLinea(String slug, CrearCitaRequest request) {
        Salon salon = buscarSalon(slug);
        return citaService.agendarCita(salon.getId(), request,
                OrigenCita.EN_LINEA, EstadoCita.PENDIENTE);
    }

    private Salon buscarSalon(String slug) {
        return salonRepository.findBySlug(slug)
                .orElseThrow(() -> new ExcepcionesNegocio.RecursoNoEncontradoException("Salón no encontrado"));
    }

    private String describirHorario(ConfiguracionSalon config) {
        if (config == null) {
            ConfiguracionSalon porDefecto = ConfiguracionSalon.porDefecto();
            return "Lunes a sábado (domingos cerrado), de " + formatearHora(porDefecto.getHoraApertura())
                    + " a " + formatearHora(porDefecto.getHoraCierre()) + ".";
        }
        String dias = Boolean.TRUE.equals(config.getAbreDomingos())
                ? "Lunes a domingo"
                : "Lunes a sábado (domingos cerrado)";
        return dias + ", de " + formatearHora(config.getHoraApertura())
                + " a " + formatearHora(config.getHoraCierre()) + ".";
    }

    private String formatearHora(LocalTime hora) {
        if (hora == null) return "";
        String periodo = hora.getHour() < 12 ? "a. m." : "p. m.";
        int hora12 = hora.getHour() % 12;
        if (hora12 == 0) hora12 = 12;
        if (hora.getMinute() == 0) {
            return hora12 + ":00 " + periodo;
        }
        return hora12 + ":" + String.format(Locale.ROOT, "%02d", hora.getMinute()) + " " + periodo;
    }
}
