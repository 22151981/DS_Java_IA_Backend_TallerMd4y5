package com.devsenior.sillon.usuario;

import com.devsenior.sillon.compartido.ExcepcionesNegocio;
import com.devsenior.sillon.salon.RegistroSalonRequest;
import com.devsenior.sillon.salon.SalonService;
import com.devsenior.sillon.seguridad.AuthRespuesta;
import com.devsenior.sillon.seguridad.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "1 Autenticación")
@RequestMapping("/api/auth")
public class AuthController {

    private final SalonService salonService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(SalonService salonService,
                          UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil) {
        this.salonService = salonService;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/registro")
    public ResponseEntity<AuthRespuesta> registrar(@Valid @RequestBody RegistroSalonRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(salonService.registrarSalon(request));
    }

    @PostMapping("/login")
    @Transactional(readOnly = true)
    public ResponseEntity<AuthRespuesta> login(@Valid @RequestBody LoginRequest request) {
        String emailNormalizado = request.email().trim().toLowerCase();
        Usuario usuario = usuarioRepository.findByEmail(emailNormalizado)
                .orElseThrow(() -> new ExcepcionesNegocio.CredencialesInvalidasException("Credenciales inválidas"));

        if (!usuario.getActivo() || !passwordEncoder.matches(request.password(), usuario.getPassword())) {
            throw new ExcepcionesNegocio.CredencialesInvalidasException("Credenciales inválidas");
        }

        String token = jwtUtil.generarToken(usuario.getEmail(), usuario.getSalon().getId(), usuario.getRol().name(), usuario.getId());
        return ResponseEntity.ok(new AuthRespuesta(
                token,
                "Bearer",
                1440L,
                usuario.getId(),
                usuario.getNombre(),
                usuario.getRol(),
                usuario.getSalon().getId(),
                usuario.getSalon().getSlug()
        ));
    }
}