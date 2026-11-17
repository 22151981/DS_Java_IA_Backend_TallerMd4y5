package com.devsenior.sillon.cita;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CitaRepository extends JpaRepository<Cita, Long> {
    @Query("SELECT c FROM Cita c WHERE c.salon.id = :salonId AND c.inicio >= :inicio AND c.inicio < :fin ORDER BY c.inicio ASC")
    List<Cita> listarPorDia(@Param("salonId") Long salonId, @Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    @Query("SELECT c FROM Cita c WHERE c.salon.id = :salonId AND c.estilista.id = :estilistaId AND c.inicio >= :inicio AND c.inicio < :fin ORDER BY c.inicio ASC")
    List<Cita> listarPorEstilistaDia(@Param("salonId") Long salonId, @Param("estilistaId") Long estilistaId, @Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    Optional<Cita> findByIdAndSalonId(Long id, Long salonId);

    @Query("SELECT COUNT(c) > 0 FROM Cita c WHERE c.salon.id = :salonId AND c.estilista.id = :estilistaId AND c.estado IN (com.devsenior.sillon.cita.EstadoCita.PENDIENTE, com.devsenior.sillon.cita.EstadoCita.CONFIRMADA) AND c.inicio < :fin AND c.fin > :inicio")
    boolean existeCruceDeHorario(@Param("salonId") Long salonId, @Param("estilistaId") Long estilistaId, @Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);
}
