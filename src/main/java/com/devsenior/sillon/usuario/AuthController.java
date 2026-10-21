package com.devsenior.sillon.usuario;

import com.devsenior.sillon.compartido.ExcepcionesNegocio;
import com.devsenior.sillon.seguridad.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginRespuesta> login(@Valid @RequestBody LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new ExcepcionesNegocio.CredencialesInvalidasException("Credenciales inválidas"));

        if (!passwordEncoder.matches(request.password(), usuario.getPassword())) {
            throw new ExcepcionesNegocio.CredencialesInvalidasException("Credenciales inválidas");
        }

        if (!usuario.getActivo()) {
            throw new ExcepcionesNegocio.AccionNoPermitidaException("Usuario inactivo");
        }

        String token = jwtUtil.generarToken(usuario.getEmail(), usuario.getSalon().getId(), usuario.getRol().name());

        return ResponseEntity.ok(new LoginRespuesta(
                token,
                usuario.getEmail(),
                usuario.getRol(),
                usuario.getSalon().getId()
        ));
    }
}