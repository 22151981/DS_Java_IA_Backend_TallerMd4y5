package com.devsenior.sillon.usuario;

import com.devsenior.sillon.catalogo.Servicio;
import com.devsenior.sillon.catalogo.ServicioRepository;
import com.devsenior.sillon.compartido.ExcepcionesNegocio;
import com.devsenior.sillon.salon.Plan;
import com.devsenior.sillon.salon.Salon;
import com.devsenior.sillon.salon.SalonRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
public class EstilistaService {

    private final UsuarioRepository usuarioRepository;
    private final SalonRepository salonRepository;
    private final ServicioRepository servicioRepository;
    private final PasswordEncoder passwordEncoder;

    public EstilistaService(UsuarioRepository usuarioRepository,
                            SalonRepository salonRepository,
                            ServicioRepository servicioRepository,
                            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.salonRepository = salonRepository;
        this.servicioRepository = servicioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public EstilistaRespuesta crearEstilista(Long salonId, CrearEstilistaRequest request) {
        Salon salon = salonRepository.findById(salonId)
                .orElseThrow(() -> new ExcepcionesNegocio.RecursoNoEncontradoException("Salón no encontrado"));

        if (salon.getPlan() == Plan.GRATIS
                && usuarioRepository.countBySalonIdAndRol(salonId, Rol.ESTILISTA) >= 2) {
            throw new ExcepcionesNegocio.PagoRequeridoException(
                    "El plan GRATIS permite un máximo de 2 estilistas");
        }

        String emailNormalizado = request.email().trim().toLowerCase();
        if (usuarioRepository.existsByEmail(emailNormalizado)) {
            throw new ExcepcionesNegocio.RecursoDuplicadoException("El correo ya está registrado");
        }

        List<Servicio> servicios = servicioRepository.findAllByIdInAndSalonId(request.servicioIds(), salonId);
        if (servicios.size() < request.servicioIds().size()) {
            throw new ExcepcionesNegocio.RecursoNoEncontradoException("Uno o más servicios no pertenecen al salón");
        }

        Usuario estilista = new Usuario();
        estilista.setNombre(request.nombre());
        estilista.setEmail(emailNormalizado);
        estilista.setPassword(passwordEncoder.encode(request.password()));
        estilista.setRol(Rol.ESTILISTA);
        estilista.setActivo(true);
        estilista.setSalon(salon);
        estilista.setServicios(new HashSet<>(servicios));

        estilista = usuarioRepository.save(estilista);
        return EstilistaRespuesta.desdeEntidad(estilista);
    }

    @Transactional(readOnly = true)
    public List<EstilistaRespuesta> listarPorSalon(Long salonId) {
        return usuarioRepository.findBySalonIdAndRol(salonId, Rol.ESTILISTA).stream()
                .map(EstilistaRespuesta::desdeEntidad)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EstilistaRespuesta> listarActivosPorSlug(String slug) {
        Salon salon = salonRepository.findBySlug(slug)
                .orElseThrow(() -> new ExcepcionesNegocio.RecursoNoEncontradoException("Salón no encontrado"));

        return usuarioRepository.findBySalonIdAndRolAndActivoTrue(salon.getId(), Rol.ESTILISTA).stream()
                .map(EstilistaRespuesta::desdeEntidad)
                .toList();
    }

    @Transactional
    public EstilistaRespuesta asignarServicios(Long salonId, Long estilistaId, AsignarServiciosRequest request) {
        Usuario estilista = usuarioRepository.findByIdAndSalonId(estilistaId, salonId)
                .orElseThrow(() -> new ExcepcionesNegocio.RecursoNoEncontradoException("Estilista no encontrado en este salón"));

        if (estilista.getRol() != Rol.ESTILISTA) {
            throw new ExcepcionesNegocio.ReglaNegocioException("El usuario seleccionado no es un estilista");
        }

        List<Servicio> serviciosValidados = servicioRepository.findAllById(request.serviciosIds());

        // Aislamiento estricto: asegurar que todos los servicios pertenezcan al mismo salón
        for (Servicio servicio : serviciosValidados) {
            if (!servicio.getSalon().getId().equals(salonId)) {
                throw new ExcepcionesNegocio.AccionNoPermitidaException("No puede asignar servicios pertenecientes a otro salón");
            }
        }

        estilista.setServicios(new HashSet<>(serviciosValidados));
        estilista = usuarioRepository.save(estilista);

        return EstilistaRespuesta.desdeEntidad(estilista);
    }

    @Transactional
    public void cambiarEstado(Long salonId, Long estilistaId, boolean activo) {
        Usuario estilista = usuarioRepository.findByIdAndSalonId(estilistaId, salonId)
                .orElseThrow(() -> new ExcepcionesNegocio.RecursoNoEncontradoException("Estilista no encontrado en este salón"));

        estilista.setActivo(activo);
        usuarioRepository.save(estilista);
    }
}
