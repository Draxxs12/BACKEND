package com.ferreteria.v1.controllers;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.ferreteria.v1.models.AuthChallenge;
import com.ferreteria.v1.models.Usuario;
import com.ferreteria.v1.models.dto.LoginRequest;
import com.ferreteria.v1.models.dto.LoginResponse;
import com.ferreteria.v1.repositories.AuthChallengeRepository;
import com.ferreteria.v1.repositories.UsuarioRepository;
import com.ferreteria.v1.security.JwtUtil;
import com.ferreteria.v1.services.EmailService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final int CODE_EXPIRATION_MINUTES = 10;
    private static final int MAX_ATTEMPTS = 5;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AuthChallengeRepository challengeRepository;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private EmailService emailService;
    @Autowired private PasswordEncoder passwordEncoder;


    @GetMapping("/me")
    public ResponseEntity<?> perfilActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sesión no válida");
        }

        Usuario usuario = usuarioRepository.findByEmail(auth.getName()).orElse(null);
        if (usuario == null || !Boolean.TRUE.equals(usuario.getActivo())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no disponible");
        }

        Map<String, Object> perfil = new java.util.LinkedHashMap<>();
        perfil.put("id", usuario.getId());
        perfil.put("nombre", usuario.getNombre());
        perfil.put("email", usuario.getEmail());
        perfil.put("rol", usuario.getRol() != null ? usuario.getRol().getNombre() : "");
        perfil.put("activo", usuario.getActivo());
        perfil.put("createdAt", usuario.getCreatedAt());
        perfil.put("registradoPor", usuario.getRegistradoPor());
        perfil.put("dobleFactorActivo", true);
        return ResponseEntity.ok(perfil);
    }

    @PostMapping("/change-password/request")
    public ResponseEntity<?> solicitarCambioPassword(@RequestBody ChangePasswordRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sesión no válida");
        }

        Usuario usuario = usuarioRepository.findByEmail(auth.getName()).orElse(null);
        if (usuario == null || !Boolean.TRUE.equals(usuario.getActivo())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no disponible");
        }
        if (request.getCurrentPassword() == null
                || !passwordEncoder.matches(request.getCurrentPassword(), usuario.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("La contraseña actual es incorrecta");
        }

        AuthChallenge challenge = crearChallenge(usuario, AuthChallenge.Type.PASSWORD_RESET);
        try {
            emailService.enviarCodigo(usuario.getEmail(), usuario.getNombre(), challengeCode(challenge), true);
        } catch (Exception e) {
            challengeRepository.delete(challenge);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("No se pudo enviar el código de verificación.");
        }

        return ResponseEntity.ok(Map.of(
                "challengeToken", challenge.getToken(),
                "email", usuario.getEmail(),
                "message", "Código de verificación enviado al correo registrado"
        ));
    }

    @PostMapping("/change-password/confirm")
    public ResponseEntity<?> confirmarCambioPassword(@RequestBody ResetPasswordRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sesión no válida");
        }

        AuthChallenge challenge = challengeRepository.findByTokenAndUsedFalse(request.getToken()).orElse(null);
        if (!valido(challenge, AuthChallenge.Type.PASSWORD_RESET)
                || !auth.getName().equalsIgnoreCase(challenge.getEmail())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("El código ha expirado o no es válido");
        }
        if (challenge.getAttempts() >= MAX_ATTEMPTS) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Demasiados intentos. Solicita un nuevo código.");
        }
        if (request.getNewPassword() == null || request.getNewPassword().length() < 8) {
            return ResponseEntity.badRequest().body("La nueva contraseña debe tener al menos 8 caracteres.");
        }

        challenge.setAttempts(challenge.getAttempts() + 1);
        if (!matches(challenge, request.getCode())) {
            challengeRepository.save(challenge);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Código incorrecto");
        }

        Usuario usuario = usuarioRepository.findByEmail(auth.getName()).orElse(null);
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no disponible");
        }

        usuario.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        usuarioRepository.save(usuario);

        challenge.setUsed(true);
        challengeRepository.save(challenge);
        return ResponseEntity.ok(Map.of("message", "Contraseña actualizada correctamente"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Correo o contraseña incorrectos");
        }

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail()).orElseThrow();
        AuthChallenge challenge = crearChallenge(usuario, AuthChallenge.Type.LOGIN_MFA);
        try {
            emailService.enviarCodigo(usuario.getEmail(), usuario.getNombre(), challengeCode(challenge),
                    false);
        } catch (Exception e) {
            challengeRepository.delete(challenge);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("No se pudo enviar el código de verificación. Configura el correo del servidor.");
        }

        return ResponseEntity.ok(Map.of(
                "requiresMfa", true,
                "challengeToken", challenge.getToken(),
                "email", usuario.getEmail(),
                "message", "Código de verificación enviado al correo registrado"
        ));
    }

    @PostMapping("/verify-mfa")
    public ResponseEntity<?> verifyMfa(@RequestBody AuthCodeRequest request) {
        AuthChallenge challenge = challengeRepository.findByTokenAndUsedFalse(request.getToken()).orElse(null);
        if (!valido(challenge, AuthChallenge.Type.LOGIN_MFA)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("El código ha expirado o no es válido");
        }
        if (challenge.getAttempts() >= MAX_ATTEMPTS) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Demasiados intentos. Solicita un nuevo código.");
        }

        challenge.setAttempts(challenge.getAttempts() + 1);
        if (!matches(challenge, request.getCode())) {
            challengeRepository.save(challenge);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Código incorrecto");
        }

        challenge.setUsed(true);
        challengeRepository.save(challenge);
        Usuario usuario = usuarioRepository.findByEmail(challenge.getEmail()).orElseThrow();
        return ResponseEntity.ok(crearLoginResponse(usuario));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail()).orElse(null);
        if (usuario == null || !Boolean.TRUE.equals(usuario.getActivo())) {
            // Evita revelar si una cuenta existe.
            return ResponseEntity.ok(Map.of("message", "Si el correo está registrado, recibirás un código de recuperación."));
        }

        AuthChallenge challenge = crearChallenge(usuario, AuthChallenge.Type.PASSWORD_RESET);
        try {
            emailService.enviarCodigo(usuario.getEmail(), usuario.getNombre(), challengeCode(challenge), true);
        } catch (Exception e) {
            challengeRepository.delete(challenge);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("No se pudo enviar el correo de recuperación. Configura el correo del servidor.");
        }
        return ResponseEntity.ok(Map.of(
                "message", "Si el correo está registrado, recibirás un código de recuperación.",
                "challengeToken", challenge.getToken()
        ));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        AuthChallenge challenge = challengeRepository.findByTokenAndUsedFalse(request.getToken()).orElse(null);
        if (!valido(challenge, AuthChallenge.Type.PASSWORD_RESET)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("El código ha expirado o no es válido");
        }
        if (challenge.getAttempts() >= MAX_ATTEMPTS) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Demasiados intentos. Solicita un nuevo código.");
        }
        if (request.getNewPassword() == null || request.getNewPassword().length() < 8) {
            return ResponseEntity.badRequest().body("La nueva contraseña debe tener al menos 8 caracteres.");
        }

        challenge.setAttempts(challenge.getAttempts() + 1);
        if (!matches(challenge, request.getCode())) {
            challengeRepository.save(challenge);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Código incorrecto");
        }

        Usuario usuario = usuarioRepository.findByEmail(challenge.getEmail()).orElseThrow();
        usuario.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        usuarioRepository.save(usuario);

        challenge.setUsed(true);
        challengeRepository.save(challenge);
        return ResponseEntity.ok(Map.of("message", "Contraseña actualizada correctamente"));
    }

    private LoginResponse crearLoginResponse(Usuario usuario) {
        String token = jwtUtil.generarToken(
                usuario.getEmail(), usuario.getRol().getNombre(), usuario.getId());
        return new LoginResponse(token, usuario.getNombre(), usuario.getEmail(), usuario.getRol().getNombre());
    }

    private AuthChallenge crearChallenge(Usuario usuario, AuthChallenge.Type type) {
        challengeRepository.deleteByEmailAndType(usuario.getEmail(), type);
        String code = String.format("%06d", RANDOM.nextInt(1_000_000));

        AuthChallenge challenge = new AuthChallenge();
        challenge.setToken(UUID.randomUUID().toString());
        challenge.setEmail(usuario.getEmail());
        challenge.setCodeHash(passwordEncoder.encode(code));
        challenge.setPlainCode(code);
        challenge.setType(type);
        challenge.setExpiresAt(LocalDateTime.now().plusMinutes(CODE_EXPIRATION_MINUTES));
        return challengeRepository.save(challenge);
    }

    private String challengeCode(AuthChallenge challenge) {
        // El código nunca se almacena en claro; se reconstruye solo para el envío
        // mediante un atributo transitorio no persistente.
        return challenge.getPlainCode();
    }

    private boolean matches(AuthChallenge challenge, String code) {
        return code != null && passwordEncoder.matches(code, challenge.getCodeHash());
    }

    private boolean valido(AuthChallenge challenge, AuthChallenge.Type type) {
        return challenge != null && challenge.getType() == type
                && challenge.getExpiresAt().isAfter(LocalDateTime.now())
                && !challenge.isUsed();
    }
}
