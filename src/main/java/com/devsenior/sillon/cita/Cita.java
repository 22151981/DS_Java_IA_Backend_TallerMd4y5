package com.devsenior.sillon.cita;

import com.devsenior.sillon.catalogo.Servicio;
import com.devsenior.sillon.cliente.Cliente;
import com.devsenior.sillon.salon.Salon;
import com.devsenior.sillon.usuario.Usuario;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "citas")
public class Cita {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private LocalDateTime inicio;
    @Column(nullable = false) private LocalDateTime fin;
    @Column(nullable = false, precision = 10, scale = 0)
    private BigDecimal precio;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private EstadoCita estado;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private OrigenCita origen;
    @Column(length = 300) private String nota;
    @Column(nullable = false) private LocalDateTime fechaCreacion;
    @Column(nullable = false) private LocalDateTime fechaActualizacion;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "salon_id", nullable = false)
    private Salon salon;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "estilista_id", nullable = false)
    private Usuario estilista;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "servicio_id", nullable = false)
    private Servicio servicio;

    public Cita() {}
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public LocalDateTime getInicio() { return inicio; } public void setInicio(LocalDateTime inicio) { this.inicio = inicio; }
    public LocalDateTime getFin() { return fin; } public void setFin(LocalDateTime fin) { this.fin = fin; }
    public BigDecimal getPrecio() { return precio; } public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public EstadoCita getEstado() { return estado; } public void setEstado(EstadoCita estado) { this.estado = estado; }
    public OrigenCita getOrigen() { return origen; } public void setOrigen(OrigenCita origen) { this.origen = origen; }
    public String getNota() { return nota; } public void setNota(String nota) { this.nota = nota; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; } public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; } public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
    public Salon getSalon() { return salon; } public void setSalon(Salon salon) { this.salon = salon; }
    public Cliente getCliente() { return cliente; } public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public Usuario getEstilista() { return estilista; } public void setEstilista(Usuario estilista) { this.estilista = estilista; }
    public Servicio getServicio() { return servicio; } public void setServicio(Servicio servicio) { this.servicio = servicio; }
}
