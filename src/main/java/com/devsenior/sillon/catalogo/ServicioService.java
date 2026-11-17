package com.devsenior.sillon.catalogo;

import com.devsenior.sillon.compartido.ExcepcionesNegocio;
import com.devsenior.sillon.salon.Salon;
import com.devsenior.sillon.salon.SalonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServicioService {

    private final ServicioRepository servicioRepository;
    private final SalonRepository salonRepository;

    public ServicioService(ServicioRepository servicioRepository, SalonRepository salonRepository) {
        this.servicioRepository = servicioRepository;
        this.salonRepository = salonRepository;
    }

    @Transactional
    public ServicioRespuesta crearServicio(Long salonId, CrearServicioRequest request) {
        Salon salon = salonRepository.findById(salonId)
                .orElseThrow(() -> new ExcepcionesNegocio.RecursoNoEncontradoException("Salón no encontrado"));

        Servicio servicio = new Servicio();
        servicio.setNombre(request.nombre());
        servicio.setDescripcion(request.descripcion());
        servicio.setDuracionMinutos(request.duracionMinutos());
        servicio.setPrecio(request.precio());
        servicio.setActivo(request.activo() == null || request.activo());
        servicio.setSalon(salon);

        servicio = servicioRepository.save(servicio);
        return ServicioRespuesta.desdeEntidad(servicio);
    }

    public List<ServicioRespuesta> listarPorSalon(Long salonId) {
        return servicioRepository.findBySalonId(salonId).stream()
                .map(ServicioRespuesta::desdeEntidad)
                .toList();
    }

    public List<ServicioRespuesta> listarActivosPorSlug(String slug) {
        Salon salon = salonRepository.findBySlug(slug)
                .orElseThrow(() -> new ExcepcionesNegocio.RecursoNoEncontradoException("Salón no encontrado"));

        return servicioRepository.findBySalonIdAndActivoTrue(salon.getId()).stream()
                .map(ServicioRespuesta::desdeEntidad)
                .toList();
    }

    @Transactional
    public ServicioRespuesta actualizarServicio(Long salonId, Long servicioId, ActualizarServicioRequest request) {
        Servicio servicio = servicioRepository.findByIdAndSalonId(servicioId, salonId)
                .orElseThrow(() -> new ExcepcionesNegocio.RecursoNoEncontradoException("Servicio no encontrado en este salón"));

        servicio.setNombre(request.nombre());
        servicio.setDescripcion(request.descripcion());
        servicio.setDuracionMinutos(request.duracionMinutos());
        servicio.setPrecio(request.precio());
        servicio.setActivo(request.activo());

        servicio = servicioRepository.save(servicio);
        return ServicioRespuesta.desdeEntidad(servicio);
    }

    @Transactional
    public void cambiarEstado(Long salonId, Long servicioId, boolean activo) {
        Servicio servicio = servicioRepository.findByIdAndSalonId(servicioId, salonId)
                .orElseThrow(() -> new ExcepcionesNegocio.RecursoNoEncontradoException("Servicio no encontrado en este salón"));
        servicio.setActivo(activo);
        servicioRepository.save(servicio);
    }
}