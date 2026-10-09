package com.ferreteria.v1.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.ferreteria.v1.models.Inventario;
import com.ferreteria.v1.models.Producto;
import com.ferreteria.v1.models.Usuario;
import com.ferreteria.v1.repositories.InventarioRepository;
import com.ferreteria.v1.repositories.ProductoRepository;

/** HU-S2-23: ajuste manual de stock con trazabilidad. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InventarioServiceTest {

    @Mock InventarioRepository inventarioRepository;
    @Mock ProductoRepository productoRepository;
    @InjectMocks InventarioService service;

    private Producto producto(int id, int stock) {
        Producto p = new Producto();
        p.setId(id);
        p.setNombre("Taladro");
        p.setStock(stock);
        return p;
    }

    private Inventario ajustar(Producto p, int nuevoStock, String motivo) {
        when(productoRepository.findById(p.getId())).thenReturn(Optional.of(p));
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(i -> i.getArgument(0));
        return service.ajustarStock(p.getId(), nuevoStock, motivo, new Usuario());
    }

    @Test
    void ajustarStock_subeElStockYRegistraElMovimiento() {
        Producto p = producto(1, 10);

        Inventario mov = ajustar(p, 20, "Conteo físico");

        assertEquals(20, p.getStock());
        assertEquals(Inventario.TipoMovimiento.Ajuste, mov.getTipo());
        assertEquals(10, mov.getStockAntes());
        assertEquals(20, mov.getStockDespues());
        assertEquals(10, mov.getCantidad());
        assertEquals("Conteo físico", mov.getMotivo());
        verify(productoRepository).save(p);
    }

    @Test
    void ajustarStock_alaBajaRegistraLaCantidadEnPositivo() {
        Producto p = producto(1, 20);

        Inventario mov = ajustar(p, 8, "Merma");

        assertEquals(8, p.getStock());
        assertEquals(12, mov.getCantidad());
        assertEquals(20, mov.getStockAntes());
        assertEquals(8, mov.getStockDespues());
    }

    @Test
    void ajustarStock_sinMotivoUsaElMotivoPorDefecto() {
        Inventario mov = ajustar(producto(1, 10), 12, null);

        assertEquals("Ajuste manual de inventario", mov.getMotivo());
    }

    @Test
    void ajustarStock_aCeroEsValido_valorLimite() {
        Producto p = producto(1, 10);

        Inventario mov = ajustar(p, 0, "Agotado");

        assertEquals(0, p.getStock());
        assertEquals(10, mov.getCantidad());
    }

    @Test
    void ajustarStock_productoInexistenteLanzaExcepcionYNoGuardaNada() {
        when(productoRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> service.ajustarStock(999, 10, "x", new Usuario()));
        verify(inventarioRepository, never()).save(any());
    }

    /**
     * INCIDENCIA ESPERADA (ROJO): hoy el servicio acepta un stock negativo y lo guarda.
     * Debe rechazarlo y no tocar el producto ni registrar el movimiento.
     */
    @Test
    void ajustarStock_rechazaUnNuevoStockNegativo() {
        Producto p = producto(1, 10);
        when(productoRepository.findById(1)).thenReturn(Optional.of(p));

        assertThrows(RuntimeException.class,
                () -> service.ajustarStock(1, -5, "Error de digitación", new Usuario()));

        assertEquals(10, p.getStock(), "el stock del producto no debe cambiar");
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }

    @Test
    void ajustarStock_elMovimientoGuardadoTieneProductoYUsuario() {
        Producto p = producto(1, 10);
        Usuario u = new Usuario();
        when(productoRepository.findById(1)).thenReturn(Optional.of(p));
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(i -> i.getArgument(0));

        service.ajustarStock(1, 15, "Reposición", u);

        ArgumentCaptor<Inventario> cap = ArgumentCaptor.forClass(Inventario.class);
        verify(inventarioRepository).save(cap.capture());
        assertSame(p, cap.getValue().getProducto());
        assertSame(u, cap.getValue().getUsuario());
    }
}
