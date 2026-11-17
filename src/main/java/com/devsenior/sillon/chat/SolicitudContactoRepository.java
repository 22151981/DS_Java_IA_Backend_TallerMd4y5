package com.devsenior.sillon.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SolicitudContactoRepository extends JpaRepository<SolicitudContacto, Long> {
    List<SolicitudContacto> findBySalonIdOrderByFechaCreacionDesc(Long salonId);
    Optional<SolicitudContacto> findByIdAndSalonId(Long id, Long salonId);
}
