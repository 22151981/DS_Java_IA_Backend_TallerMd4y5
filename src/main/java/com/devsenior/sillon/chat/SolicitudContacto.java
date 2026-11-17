package com.devsenior.sillon.chat;

import com.devsenior.sillon.salon.Salon;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitudes_contacto")
public class SolicitudContacto {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String clienteNombre;
    @Column(nullable = false, length = 120)
    private String clienteContacto;
    @Column(length = 2000)
    private String mensajes;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private MotivoSolicitud motivo;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private EstadoSolicitud estado;
    @Column(nullable = false)
    private LocalDateTime fechaCreacion;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "salon_id", nullable = false)
    private Salon salon;

    public SolicitudContacto() {}
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getClienteNombre() { return clienteNombre; } public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }
    public String getClienteContacto() { return clienteContacto; } public void setClienteContacto(String clienteContacto) { this.clienteContacto = clienteContacto; }
    public String getMensajes() { return mensajes; } public void setMensajes(String mensajes) { this.mensajes = mensajes; }
    public MotivoSolicitud getMotivo() { return motivo; } public void setMotivo(MotivoSolicitud motivo) { this.motivo = motivo; }
    public EstadoSolicitud getEstado() { return estado; } public void setEstado(EstadoSolicitud estado) { this.estado = estado; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; } public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public Salon getSalon() { return salon; } public void setSalon(Salon salon) { this.salon = salon; }

    public SolicitudRespuesta aRespuesta() {
        return new SolicitudRespuesta(id, clienteNombre, clienteContacto, mensajes, motivo, estado, fechaCreacion);
    }
}
