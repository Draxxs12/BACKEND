package com.ferreteria.v1.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.ferreteria.v1.models.Usuario;
import com.ferreteria.v1.repositories.UsuarioRepository;

/**
 * Devuelve el nombre del usuario autenticado (del token JWT) para auditoría.
 */
@Service
public class UsuarioActualService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public String nombreActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) return "Sistema";
        String email = auth.getName();
        return usuarioRepository.findByEmail(email).map(Usuario::getNombre).orElse(email);
    }
}
