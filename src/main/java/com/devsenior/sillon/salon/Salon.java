package com.devsenior.sillon.salon;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "salones")
public class Salon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, unique = true, length = 120)
    private String slug;

    @Column(nullable = false, length = 60)
    private String ciudad;

    @Column(nullable = false, length = 150)
    private String direccion;

    @Column(nullable = false, length = 30)
    private String telefono;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Plan plan;

    @Column(nullable = false)
    private Integer creditosIa;

    @Column(length = 100)
    private String stripeSuscripcionId;

    @Embedded
    private ConfiguracionSalon configuracion;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    public Salon() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public Plan getPlan() {
        return plan;
    }

    public void setPlan(Plan plan) {
        this.plan = plan;
    }

    public Integer getCreditosIa() {
        return creditosIa;
    }

    public void setCreditosIa(Integer creditosIa) {
        this.creditosIa = creditosIa;
    }

    public String getStripeSuscripcionId() {
        return stripeSuscripcionId;
    }

    public void setStripeSuscripcionId(String stripeSuscripcionId) {
        this.stripeSuscripcionId = stripeSuscripcionId;
    }

    public ConfiguracionSalon getConfiguracion() {
        return configuracion;
    }

    public void setConfiguracion(ConfiguracionSalon configuracion) {
        this.configuracion = configuracion;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}