package com.devsenior.sillon.salon;

import com.devsenior.sillon.compartido.ExcepcionesNegocio;
import com.devsenior.sillon.seguridad.AuthRespuesta;
import com.devsenior.sillon.seguridad.JwtUtil;
import com.devsenior.sillon.usuario.Rol;
import com.devsenior.sillon.usuario.Usuario;
import com.devsenior.sillon.usuario.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
public class SalonService {

    private final SalonRepository salonRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;
    private final JwtUtil jwtUtil;
    private final int creditosIniciales;

    public SalonService(SalonRepository salonRepository,
                        UsuarioRepository usuarioRepository,
                        PasswordEncoder passwordEncoder,
                        Clock clock,
                        JwtUtil jwtUtil,
                        @Value("${sillon.creditos.registro:10}") int creditosIniciales) {
        this.salonRepository = salonRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
        this.jwtUtil = jwtUtil;
        this.creditosIniciales = creditosIniciales;
    }

    @Transactional
    public AuthRespuesta registrarSalon(RegistroSalonRequest request) {
        String emailNormalizado = request.email().trim().toLowerCase();
        if (usuarioRepository.existsByEmail(emailNormalizado)) {
            throw new ExcepcionesNegocio.RecursoDuplicadoException("El correo ya está registrado");
        }

        String slug = generarSlugUnico(request.salonNombre());

        Salon salon = new Salon();
        salon.setNombre(request.salonNombre());
        salon.setSlug(slug);
        salon.setCiudad(request.ciudad());
        salon.setDireccion(request.direccion());
        salon.setTelefono(request.telefono());
        salon.setPlan(Plan.GRATIS);
        salon.setCreditosIa(creditosIniciales);
        salon.setConfiguracion(ConfiguracionSalon.porDefecto());
        salon.setFechaCreacion(LocalDateTime.now(clock));

        salon = salonRepository.save(salon);

        Usuario dueno = new Usuario();
        dueno.setNombre(request.nombre());
        dueno.setEmail(emailNormalizado);
        dueno.setPassword(passwordEncoder.encode(request.password()));
        dueno.setRol(Rol.DUENO);
        dueno.setActivo(true);
        dueno.setSalon(salon);

        dueno = usuarioRepository.save(dueno);

        String token = jwtUtil.generarToken(dueno.getEmail(), salon.getId(), dueno.getRol().name());

        return new AuthRespuesta(
                token,
                "Bearer",
                60L, // o los minutos configurados en tu JwtUtil
                dueno.getId(),
                dueno.getNombre(),
                dueno.getRol(),
                salon.getId(),
                salon.getSlug()
        );
    }

    public SalonRespuesta obtenerPorSlug(String slug) {
        Salon salon = salonRepository.findBySlug(slug)
                .orElseThrow(() -> new ExcepcionesNegocio.RecursoNoEncontradoException("Salón no encontrado"));
        return SalonRespuesta.desdeEntidad(salon);
    }

    private String generarSlugUnico(String nombre) {
        String base = SlugUtil.generarSlugBase(nombre);
        String candidato = base;
        int contador = 2;
        while (salonRepository.existsBySlug(candidato)) {
            candidato = base + "-" + contador;
            contador++;
        }
        return candidato;
    }

    public ConfiguracionSalon obtenerConfiguracionPorId(Long salonId) {
        Salon salon = salonRepository.findById(salonId)
                .orElseThrow(() -> new ExcepcionesNegocio.RecursoNoEncontradoException("Salón no encontrado"));
        return salon.getConfiguracion();
    }

    @Transactional
    public ConfiguracionSalon actualizarConfiguracion(Long salonId, ConfiguracionSalonRequest request) {
        Salon salon = salonRepository.findById(salonId)
                .orElseThrow(() -> new ExcepcionesNegocio.RecursoNoEncontradoException("Salón no encontrado"));

        if (request.horaCierre().isBefore(request.horaApertura()) || request.horaCierre().equals(request.horaApertura())) {
            throw new ExcepcionesNegocio.ReglaNegocioException("La hora de cierre debe ser posterior a la de apertura");
        }

        ConfiguracionSalon config = salon.getConfiguracion();
        if (config == null) {
            config = new ConfiguracionSalon();
        }
        config.setHoraApertura(request.horaApertura());
        config.setHoraCierre(request.horaCierre());
        config.setAbreDomingos(request.abreDomingos());
        config.setIntervaloMinutos(request.intervaloMinutos());

        salon.setConfiguracion(config);
        salonRepository.save(salon);

        return config;
    }
}