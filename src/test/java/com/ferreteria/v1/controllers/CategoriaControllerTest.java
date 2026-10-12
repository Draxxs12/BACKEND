package com.ferreteria.v1.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.ferreteria.v1.exceptions.ValidacionException;
import com.ferreteria.v1.models.Categoria;
import com.ferreteria.v1.repositories.CategoriaRepository;
import com.ferreteria.v1.services.UsuarioActualService;

/**
 * HU-S2-18 Listar categorías, HU-S2-19 Registrar categoría, HU-S2-20 Editar categoría (H0051-H0054).
 * Las categorías no tienen servicio: el controlador habla directo con el repositorio, por eso se prueba aquí.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CategoriaControllerTest {

    @Mock CategoriaRepository categoriaRepository;
    @Mock UsuarioActualService usuarioActual;
    @InjectMocks CategoriaController controller;

    private Categoria categoria(Integer id, String nombre) {
        Categoria c = new Categoria();
        c.setId(id);
        c.setNombre(nombre);
        return c;
    }

    // ───────────── HU-S2-18 Listar ─────────────
    @Test
    void listar_devuelveTodasLasCategoriasDelRepositorio() {
        when(categoriaRepository.findAll()).thenReturn(List.of(categoria(1, "Herramientas"), categoria(2, "Pinturas")));

        List<Categoria> r = controller.listar();

        assertEquals(2, r.size());
        assertEquals("Herramientas", r.get(0).getNombre());
    }

    @Test
    void listar_sinCategoriasDevuelveListaVaciaSinError() {
        when(categoriaRepository.findAll()).thenReturn(List.of());

        assertTrue(controller.listar().isEmpty());
    }

    @Test
    void buscar_categoriaExistenteDevuelve200ConLaCategoria() {
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria(1, "Herramientas")));

        ResponseEntity<Categoria> r = controller.buscar(1);

        assertEquals(HttpStatus.OK, r.getStatusCode());
        assertEquals("Herramientas", r.getBody().getNombre());
    }

    @Test
    void buscar_categoriaInexistenteDevuelve404() {
        when(categoriaRepository.findById(99)).thenReturn(Optional.empty());

        assertEquals(HttpStatus.NOT_FOUND, controller.buscar(99).getStatusCode());
    }

    // ───────────── HU-S2-19 Registrar ─────────────
    @Test
    void crear_guardaLaCategoriaConElUsuarioQueLaRegistra() {
        when(usuarioActual.nombreActual()).thenReturn("Dany");
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(i -> i.getArgument(0));

        Categoria r = controller.crear(categoria(null, "Pinturas"));

        assertEquals("Pinturas", r.getNombre());
        assertEquals("Dany", r.getRegistradoPor());
        verify(categoriaRepository).save(any(Categoria.class));
    }

    /**
     * INCIDENCIA ESPERADA (ROJO): hoy el servidor acepta una categoría sin nombre y la guarda;
     * solo la pantalla Angular lo impide. Debe rechazarse en el servidor (lección 3 del laboratorio).
     * Corrección sugerida: validar que el nombre no esté en blanco antes del save y lanzar ValidacionException.
     */
    @Test
    void crear_rechazaUnNombreEnBlancoYNoGuarda() {
        assertThrows(ValidacionException.class, () -> controller.crear(categoria(null, "   ")));

        verify(categoriaRepository, never()).save(any());
    }

    // ───────────── HU-S2-20 Editar ─────────────
    @Test
    void actualizar_usaElIdDeLaRutaAunqueElCuerpoTraigaOtro() {
        when(usuarioActual.nombreActual()).thenReturn("Dany");
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(i -> i.getArgument(0));

        ResponseEntity<Categoria> r = controller.actualizar(5, categoria(77, "Pinturas y barnices"));

        assertEquals(HttpStatus.OK, r.getStatusCode());
        assertEquals(5, r.getBody().getId());
        assertEquals("Pinturas y barnices", r.getBody().getNombre());
    }

    @Test
    void actualizar_registraAQuienHizoElCambio() {
        when(usuarioActual.nombreActual()).thenReturn("Ronald");
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(i -> i.getArgument(0));

        Categoria r = controller.actualizar(5, categoria(null, "X")).getBody();

        assertEquals("Ronald", r.getRegistradoPor());
    }

    // ───────────── H0054 Eliminar ─────────────
    @Test
    void eliminar_borraPorIdYDevuelve204() {
        ResponseEntity<Void> r = controller.eliminar(3);

        assertEquals(HttpStatus.NO_CONTENT, r.getStatusCode());
        verify(categoriaRepository).deleteById(3);
    }
}
