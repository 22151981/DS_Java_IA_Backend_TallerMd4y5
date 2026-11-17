package com.devsenior.sillon.agenda;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    List<Cita> findBySalonIdOrderByFechaHoraInicioAsc(Long salonId);

    List<Cita> findBySalonIdAndEstilistaIdOrderByFechaHoraInicioAsc(Long salonId, Long estilistaId);

    Optional<Cita> findByIdAndSalonId(Long id, Long salonId);

    @Query("""
            SELECT c FROM Cita c
            WHERE c.estilista.id = :estilistaId
              AND c.salon.id = :salonId
              AND c.estado IN (com.devsenior.sillon.agenda.EstadoCita.PENDIENTE, com.devsenior.sillon.agenda.EstadoCita.CONFIRMADA)
              AND c.fechaHoraInicio < :finDia
              AND c.fechaHoraFin > :inicioDia
            ORDER BY c.fechaHoraInicio ASC
            """)
    List<Cita> findCitasActivasEnRango(
            @Param("salonId") Long salonId,
            @Param("estilistaId") Long estilistaId,
            @Param("inicioDia") LocalDateTime inicioDia,
            @Param("finDia") LocalDateTime finDia);

    @Query("""
            SELECT COUNT(c) > 0 FROM Cita c
            WHERE c.estilista.id = :estilistaId
              AND c.salon.id = :salonId
              AND c.estado IN (com.devsenior.sillon.agenda.EstadoCita.PENDIENTE, com.devsenior.sillon.agenda.EstadoCita.CONFIRMADA)
              AND c.fechaHoraInicio < :fin
              AND c.fechaHoraFin > :inicio
            """)
    boolean existeCruceDeHorario(
            @Param("salonId") Long salonId,
            @Param("estilistaId") Long estilistaId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);
}