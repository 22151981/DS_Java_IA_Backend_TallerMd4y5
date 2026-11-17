package com.devsenior.sillon.ia;

import com.devsenior.sillon.compartido.ExcepcionesNegocio;
import com.devsenior.sillon.salon.Salon;
import com.devsenior.sillon.salon.SalonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreditoIaService {
    private final SalonRepository salonRepository;

    public CreditoIaService(SalonRepository salonRepository) {
        this.salonRepository = salonRepository;
    }

    public void verificarDisponibilidad(Long salonId) {
        Salon salon = salonRepository.findById(salonId)
                .orElseThrow(() -> new ExcepcionesNegocio.RecursoNoEncontradoException("Salón no encontrado"));
        if (salon.getCreditosIa() == null || salon.getCreditosIa() <= 0) {
            throw new ExcepcionesNegocio.PagoRequeridoException("Créditos de IA agotados");
        }
    }

    @Transactional
    public int consumirCredito(Long salonId) {
        int filas = salonRepository.consumirCredito(salonId);
        if (filas == 0) throw new ExcepcionesNegocio.PagoRequeridoException("Créditos de IA agotados");
        salonRepository.flush();
        return salonRepository.findById(salonId)
                .map(Salon::getCreditosIa).orElseThrow(() ->
                        new ExcepcionesNegocio.RecursoNoEncontradoException("Salón no encontrado"));
    }
}
