package com.devsenior.sillon.cliente;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClienteService {
    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public List<ClienteRespuesta> listarPorSalon(Long salonId) {
        return clienteRepository.findBySalonId(salonId).stream()
                .map(ClienteRespuesta::desdeEntidad)
                .toList();
    }
}
