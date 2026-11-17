package com.devsenior.sillon.catalogo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServicioRepository extends JpaRepository<Servicio, Long> {

    List<Servicio> findBySalonId(Long salonId);

    List<Servicio> findBySalonIdAndActivoTrue(Long salonId);

    Optional<Servicio> findByIdAndSalonId(Long id, Long salonId);

    List<Servicio> findAllByIdInAndSalonId(Iterable<Long> ids, Long salonId);
}