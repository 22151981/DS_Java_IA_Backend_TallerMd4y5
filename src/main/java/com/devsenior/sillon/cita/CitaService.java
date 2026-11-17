package com.devsenior.sillon.cita;

import com.devsenior.sillon.catalogo.Servicio;
import com.devsenior.sillon.catalogo.ServicioRepository;
import com.devsenior.sillon.cliente.Cliente;
import com.devsenior.sillon.cliente.ClienteRepository;
import com.devsenior.sillon.compartido.ExcepcionesNegocio;
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
import java.util.List;

@Service
public class CitaService {
    private final CitaRepository citaRepository;
    private final ServicioRepository servicioRepository;
    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final SalonRepository salonRepository;
    private final ReglasAgenda reglasAgenda;
    private final Clock clock;

    public CitaService(CitaRepository citaRepository, ServicioRepository servicioRepository,
                       UsuarioRepository usuarioRepository, ClienteRepository clienteRepository,
                       SalonRepository salonRepository, ReglasAgenda reglasAgenda, Clock clock) {
        this.citaRepository = citaRepository;
        this.servicioRepository = servicioRepository;
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.salonRepository = salonRepository;
        this.reglasAgenda = reglasAgenda;
        this.clock = clock;
    }

    @Transactional
    public CitaRespuesta agendarCita(Long salonId, CrearCitaRequest request, OrigenCita origen, EstadoCita estadoInicial) {
        Servicio servicio = servicioRepository.findByIdAndSalonId(request.servicioId(), salonId)
                .filter(Servicio::getActivo)
                .orElseThrow(() -> new ExcepcionesNegocio.RecursoNoEncontradoException("Servicio no encontrado en este salón"));
        Usuario estilista = usuarioRepository.findByIdAndSalonId(request.estilistaId(), salonId)
                .filter(u -> u.getRol() == Rol.ESTILISTA && Boolean.TRUE.equals(u.getActivo()))
                .orElseThrow(() -> new ExcepcionesNegocio.RecursoNoEncontradoException("Estilista no encontrado en este salón"));
        if (!estilista.getServicios().contains(servicio)) {
            throw new ExcepcionesNegocio.ReglaNegocioException("El estilista no ofrece este servicio");
        }
        LocalDateTime inicio = request.inicio();
        LocalDateTime fin = inicio.plusMinutes(servicio.getDuracionMinutos());
        if (inicio.isBefore(LocalDateTime.now(clock))) {
            throw new ExcepcionesNegocio.ReglaNegocioException("No puede agendar una cita en el pasado");
        }
        Salon salon = salonRepository.findById(salonId)
                .orElseThrow(() -> new ExcepcionesNegocio.RecursoNoEncontradoException("Salón no encontrado"));
        reglasAgenda.validarHorario(inicio, fin, salon.getConfiguracion());
        if (citaRepository.existeCruceDeHorario(salonId, estilista.getId(), inicio, fin)) {
            throw new ExcepcionesNegocio.ReglaNegocioException("El horario seleccionado cruza con otra cita del estilista");
        }
        String telefono = request.clienteTelefono().replaceAll("[^0-9+]", "");
        Cliente cliente = clienteRepository.findBySalonId(salonId).stream()
                .filter(c -> c.getTelefono().equals(telefono)).findFirst().orElse(null);
        if (cliente == null) {
            cliente = new Cliente(); cliente.setNombre(request.clienteNombre()); cliente.setTelefono(telefono); cliente.setSalon(salon);
            cliente = clienteRepository.save(cliente);
        } else {
            cliente.setNombre(request.clienteNombre());
            clienteRepository.save(cliente);
        }
        Cita cita = new Cita();
        cita.setSalon(salon); cita.setCliente(cliente); cita.setEstilista(estilista); cita.setServicio(servicio);
        cita.setInicio(inicio); cita.setFin(fin); cita.setPrecio(servicio.getPrecio());
        cita.setEstado(estadoInicial); cita.setOrigen(origen); cita.setNota(request.nota());
        LocalDateTime ahora = LocalDateTime.now(clock);
        cita.setFechaCreacion(ahora); cita.setFechaActualizacion(ahora);
        return CitaRespuesta.desdeEntidad(citaRepository.save(cita));
    }

    public List<CitaRespuesta> listarCitasPorDia(Long salonId, Long usuarioId, Rol rol, LocalDate fechaOpcional) {
        LocalDate fecha = fechaOpcional == null ? LocalDate.now(clock) : fechaOpcional;
        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin = fecha.plusDays(1).atStartOfDay();
        List<Cita> citas = rol == Rol.DUENO
                ? citaRepository.listarPorDia(salonId, inicio, fin)
                : citaRepository.listarPorEstilistaDia(salonId, usuarioId, inicio, fin);
        return citas.stream().map(CitaRespuesta::desdeEntidad).toList();
    }

    @Transactional
    public CitaRespuesta cambiarEstado(Long salonId, Long usuarioId, Rol rol, Long citaId, EstadoCita nuevoEstado) {
        Cita cita = citaRepository.findByIdAndSalonId(citaId, salonId)
                .orElseThrow(() -> new ExcepcionesNegocio.RecursoNoEncontradoException("Cita no encontrada en este salón"));
        if (rol == Rol.ESTILISTA) {
            if (!cita.getEstilista().getId().equals(usuarioId)) {
                throw new ExcepcionesNegocio.RecursoNoEncontradoException("Cita no encontrada en este salón");
            }
            if (nuevoEstado != EstadoCita.COMPLETADA && nuevoEstado != EstadoCita.NO_ASISTIO) {
                throw new ExcepcionesNegocio.AccionNoPermitidaException("El estilista solo puede completar la cita o marcarla como no asistió");
            }
        }
        if (!cita.getEstado().puedeTransicionarA(nuevoEstado)) {
            throw new ExcepcionesNegocio.ReglaNegocioException("La transición de estado no está permitida");
        }
        cita.setEstado(nuevoEstado); cita.setFechaActualizacion(LocalDateTime.now(clock));
        return CitaRespuesta.desdeEntidad(citaRepository.saveAndFlush(cita));
    }
}
