package com.ferreteria.v1.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ferreteria.v1.models.dto.LoginRequest;
import com.ferreteria.v1.models.dto.LoginResponse;

import com.ferreteria.v1.security.JwtUtil;
import com.ferreteria.v1.models.Usuario;
import com.ferreteria.v1.repositories.UsuarioRepository;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // Valida email + password contra la BD (usa BCrypt internamente)
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Correo o contraseña incorrectos");
        }

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow();

        String token = jwtUtil.generarToken(
                usuario.getEmail(),
                usuario.getRol().getNombre(),
                usuario.getId()
        );

        LoginResponse response = new LoginResponse(
                token,
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getRol().getNombre()
        );

        return ResponseEntity.ok(response);
    }
}