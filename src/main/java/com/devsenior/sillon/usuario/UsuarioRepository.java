package com.devsenior.sillon.usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    @EntityGraph(attributePaths = {"salon", "servicios"})
    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    @EntityGraph(attributePaths = "servicios")
    List<Usuario> findBySalonIdAndRol(Long salonId, Rol rol);

    @EntityGraph(attributePaths = "servicios")
    List<Usuario> findBySalonIdAndRolAndActivoTrue(Long salonId, Rol rol);

    @EntityGraph(attributePaths = {"salon", "servicios"})
    Optional<Usuario> findByIdAndSalonId(Long id, Long salonId);

    long countBySalonIdAndRol(Long salonId, Rol rol);
}