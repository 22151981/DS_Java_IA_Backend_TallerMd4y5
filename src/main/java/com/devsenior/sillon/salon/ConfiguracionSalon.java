package com.devsenior.sillon.salon;

import jakarta.persistence.Embeddable;
import java.time.LocalTime;

@Embeddable
public class ConfiguracionSalon {

    private LocalTime horaApertura;
    private LocalTime horaCierre;
    private Boolean abreDomingos;
    private Integer intervaloMinutos;

    public ConfiguracionSalon() {
    }

    public ConfiguracionSalon(LocalTime horaApertura, LocalTime horaCierre, Boolean abreDomingos, Integer intervaloMinutos) {
        this.horaApertura = horaApertura;
        this.horaCierre = horaCierre;
        this.abreDomingos = abreDomingos;
        this.intervaloMinutos = intervaloMinutos;
    }

    public static ConfiguracionSalon porDefecto() {
        return new ConfiguracionSalon(
                LocalTime.of(8, 0),
                LocalTime.of(19, 0),
                false,
                30
        );
    }

    public LocalTime getHoraApertura() {
        return horaApertura;
    }

    public void setHoraApertura(LocalTime horaApertura) {
        this.horaApertura = horaApertura;
    }

    public LocalTime getHoraCierre() {
        return horaCierre;
    }

    public void setHoraCierre(LocalTime horaCierre) {
        this.horaCierre = horaCierre;
    }

    public Boolean getAbreDomingos() {
        return abreDomingos;
    }

    public void setAbreDomingos(Boolean abreDomingos) {
        this.abreDomingos = abreDomingos;
    }

    public Integer getIntervaloMinutos() {
        return intervaloMinutos;
    }

    public void setIntervaloMinutos(Integer intervaloMinutos) {
        this.intervaloMinutos = intervaloMinutos;
    }
}