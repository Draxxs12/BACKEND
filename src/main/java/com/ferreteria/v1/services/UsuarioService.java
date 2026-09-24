package com.ferreteria.v1.services;

import com.ferreteria.v1.exceptions.Validador;
import com.ferreteria.v1.models.Rol;
import com.ferreteria.v1.models.Usuario;
import com.ferreteria.v1.repositories.RolRepository;
import com.ferreteria.v1.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UsuarioService {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private RolRepository rolRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private UsuarioActualService usuarioActual;

    public List<Usuario> listarActivos() {
        return usuarioRepository.findByActivoTrue();
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario buscarPorId(Integer id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email).orElse(null);
    }

    /** Crea un usuario nuevo cifrando la contraseña con BCrypt. */
    public Usuario crear(String nombre, String email, String password, Integer rolId, Boolean activo) {
        Validador.email(email);
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode(password != null ? password : "123456"));
        u.setRol(resolverRol(rolId));
        u.setActivo(activo != null ? activo : true);
        u.setRegistradoPor(usuarioActual.nombreActual());
        return usuarioRepository.save(u);
    }

    /** Actualiza datos; solo cambia la contraseña si se envía una nueva. */
    public Usuario actualizar(Integer id, String nombre, String email, String password, Integer rolId, Boolean activo) {
        Validador.email(email);
        Usuario u = usuarioRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Usuario no encontrado id=" + id));
        if (nombre != null) u.setNombre(nombre);
        if (email != null)  u.setEmail(email);
        if (rolId != null)  u.setRol(resolverRol(rolId));
        if (activo != null) u.setActivo(activo);
        if (password != null && !password.isBlank()) {
            u.setPasswordHash(passwordEncoder.encode(password));
        }
        u.setRegistradoPor(usuarioActual.nombreActual());
        return usuarioRepository.save(u);
    }

    public Usuario guardar(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    public void eliminar(Integer id) {
        usuarioRepository.findById(id).ifPresent(u -> {
            u.setActivo(false);
            usuarioRepository.save(u);
        });
    }

    private Rol resolverRol(Integer rolId) {
        if (rolId == null) return null;
        return rolRepository.findById(rolId).orElse(null);
    }
}
