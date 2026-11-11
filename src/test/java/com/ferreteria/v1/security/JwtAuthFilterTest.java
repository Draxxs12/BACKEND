package com.ferreteria.v1.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;

class JwtAuthFilterTest {

    private JwtUtil jwtUtil;
    private CustomUserDetailsService userDetailsService;
    private JwtAuthFilter jwtAuthFilter;
    private FilterChain filterChain;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        jwtUtil = mock(JwtUtil.class);
        userDetailsService = mock(CustomUserDetailsService.class);
        jwtAuthFilter = new JwtAuthFilter();

        ReflectionTestUtils.setField(jwtAuthFilter, "jwtUtil", jwtUtil);
        ReflectionTestUtils.setField(jwtAuthFilter, "userDetailsService", userDetailsService);

        filterChain = mock(FilterChain.class);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void debeAutenticarCuandoElTokenYElUsuarioSonValidos() throws Exception {
        String token = "token-valido";
        String email = "usuario@ferreteria.com";
        UserDetails userDetails = User.withUsername(email)
                .password("password")
                .authorities("ROLE_USER")
                .build();

        request.addHeader("Authorization", "Bearer " + token);
        when(jwtUtil.extraerEmail(token)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);

        jwtAuthFilter.doFilter(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(email, authentication.getName());
        verify(jwtUtil).extraerEmail(token);
        verify(userDetailsService).loadUserByUsername(email);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void debeContinuarSinAutenticarCuandoElTokenEsInvalido() throws Exception {
        String token = "token-invalido";
        request.addHeader("Authorization", "Bearer " + token);
        when(jwtUtil.extraerEmail(token)).thenThrow(new MalformedJwtException("Token inválido"));

        jwtAuthFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(userDetailsService, never()).loadUserByUsername(org.mockito.ArgumentMatchers.anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void debeContinuarSinAutenticarCuandoElUsuarioNoExisteOEstaInactivo() throws Exception {
        String token = "token-valido";
        String email = "inactivo@ferreteria.com";
        request.addHeader("Authorization", "Bearer " + token);
        when(jwtUtil.extraerEmail(token)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email))
                .thenThrow(new UsernameNotFoundException("Usuario no disponible"));

        jwtAuthFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(userDetailsService).loadUserByUsername(email);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void debeContinuarSinAutenticarCuandoNoHayToken() throws Exception {
        jwtAuthFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtUtil, never()).extraerEmail(org.mockito.ArgumentMatchers.anyString());
        verify(userDetailsService, never()).loadUserByUsername(org.mockito.ArgumentMatchers.anyString());
        verify(filterChain).doFilter(request, response);
    }
}
