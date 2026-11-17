package com.devsenior.sillon.usuario;

import com.devsenior.sillon.catalogo.Servicio;
import com.devsenior.sillon.catalogo.ServicioRepository;
import com.devsenior.sillon.compartido.ExcepcionesNegocio;
import com.devsenior.sillon.salon.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EstilistaServiceTest {
    @Mock UsuarioRepository usuarios; @Mock SalonRepository salones; @Mock ServicioRepository servicios; @Mock PasswordEncoder encoder;
    @InjectMocks EstilistaService service;

    @Test void gratisTercerEstilistaLanza402() {
        Salon salon = new Salon(); salon.setPlan(Plan.GRATIS);
        when(salones.findById(1L)).thenReturn(Optional.of(salon));
        when(usuarios.countBySalonIdAndRol(1L,Rol.ESTILISTA)).thenReturn(2L);
        assertThrows(ExcepcionesNegocio.PagoRequeridoException.class, () -> service.crearEstilista(1L,new CrearEstilistaRequest("Ana","ana@test.com","password1",List.of(1L))));
        verify(usuarios, never()).save(any());
    }
    @Test void proPermiteCrearEstilista() {
        Salon salon = new Salon(); salon.setId(1L); salon.setPlan(Plan.PRO);
        Servicio servicio = new Servicio(); servicio.setId(1L);
        when(salones.findById(1L)).thenReturn(Optional.of(salon));
        when(usuarios.existsByEmail("ana@test.com")).thenReturn(false);
        when(servicios.findAllByIdInAndSalonId(List.of(1L),1L)).thenReturn(List.of(servicio));
        when(encoder.encode("password1")).thenReturn("hash");
        when(usuarios.save(any())).thenAnswer(i -> i.getArgument(0));
        assertDoesNotThrow(() -> service.crearEstilista(1L,new CrearEstilistaRequest("Ana","ana@test.com","password1",List.of(1L))));
    }
}
