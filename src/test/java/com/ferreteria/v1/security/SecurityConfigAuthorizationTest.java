package com.ferreteria.v1.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "jwt.secret=QUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUE=")
@AutoConfigureMockMvc
class SecurityConfigAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "RECEPCIONISTA")
    void recepcionistaNoPuedeAdministrarUsuarios() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "RECEPCIONISTA")
    void recepcionistaNoPuedeAdministrarInventario() throws Exception {
        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ALMACENERO")
    void almaceneroNoPuedeConsultarClientes() throws Exception {
        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ALMACENERO")
    void almaceneroNoPuedeConsultarVentas() throws Exception {
        mockMvc.perform(get("/api/ventas"))
                .andExpect(status().isForbidden());
    }

    @Test
    void reenvioMfaNoExigeSesionJwt() throws Exception {
        mockMvc.perform(post("/api/auth/resend-mfa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"token-inexistente\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "La solicitud de verificación ya no es válida")));
    }
}
