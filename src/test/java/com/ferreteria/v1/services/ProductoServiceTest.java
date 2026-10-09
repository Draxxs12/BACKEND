package com.ferreteria.v1.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.ferreteria.v1.exceptions.ValidacionException;
import com.ferreteria.v1.models.Producto;
import com.ferreteria.v1.repositories.ProductoRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProductoServiceTest {

    @Mock ProductoRepository productoRepository;
    @Mock UsuarioActualService usuarioActual;
    @InjectMocks ProductoService service;

    private Producto producto(Integer id, int stock, int stockMinimo, double compra, double venta) {
        Producto p = new Producto();
        p.setId(id);
        p.setNombre("Producto " + id);
        p.setStock(stock);
        p.setStockMinimo(stockMinimo);
        p.setPrecioCompra(compra);
        p.setPrecioVenta(venta);
        p.setActivo(true);
        return p;
    }

    // ───────────── HU-S2-15 Listar productos activos ─────────────
    @Test
    void listarActivos_soloPideLosActivos() {
        Producto p = producto(1, 10, 5, 10, 15);
        when(productoRepository.findByActivoTrue()).thenReturn(List.of(p));

        assertEquals(1, service.listarActivos().size());
        verify(productoRepository).findByActivoTrue();
    }

    // ───────────── HU-S2-16 Buscar por nombre ─────────────
    @Test
    void buscarPorNombre_delegaEnLaBusquedaSinDistinguirMayusculas() {
        Producto p = producto(1, 10, 5, 10, 15);
        p.setNombre("Martillo");
        when(productoRepository.findByActivoTrueAndNombreContainingIgnoreCase("martillo"))
                .thenReturn(List.of(p));

        List<Producto> r = service.buscarPorNombre("martillo");

        assertEquals(1, r.size());
        assertEquals("Martillo", r.get(0).getNombre());
    }

    // ───────────── HU-S2-17 Registrar producto ─────────────
    @Test
    void guardar_productoValidoSePersisteConElUsuarioQueLoRegistra() {
        Producto p = producto(null, 10, 5, 10, 15);
        when(usuarioActual.nombreActual()).thenReturn("Dany");
        when(productoRepository.save(any(Producto.class))).thenAnswer(i -> i.getArgument(0));

        Producto guardado = service.guardar(p);

        assertEquals("Dany", guardado.getRegistradoPor());
        verify(productoRepository).save(p);
    }

    @Test
    void guardar_ventaIgualAComprasEsValido_valorLimite() {
        Producto p = producto(null, 0, 0, 10, 10);
        when(productoRepository.save(any(Producto.class))).thenAnswer(i -> i.getArgument(0));

        assertDoesNotThrow(() -> service.guardar(p));
    }

    @Test
    void guardar_rechazaPrecioDeVentaMenorQueCompra() {
        Producto p = producto(null, 10, 5, 100, 50);

        assertThrows(ValidacionException.class, () -> service.guardar(p));
        verify(productoRepository, never()).save(any());
    }

    @Test
    void guardar_rechazaStockNegativo() {
        Producto p = producto(null, -1, 5, 10, 15);

        assertThrows(ValidacionException.class, () -> service.guardar(p));
        verify(productoRepository, never()).save(any());
    }

    @Test
    void guardar_rechazaStockMinimoNegativo() {
        Producto p = producto(null, 10, -1, 10, 15);

        assertThrows(ValidacionException.class, () -> service.guardar(p));
        verify(productoRepository, never()).save(any());
    }

    @Test
    void guardar_rechazaPrecioDeCompraNegativo() {
        Producto p = producto(null, 10, 5, -10, 15);

        assertThrows(ValidacionException.class, () -> service.guardar(p));
        verify(productoRepository, never()).save(any());
    }

    // ───────────── HU-S2-05 Stock crítico ─────────────
    /**
     * INCIDENCIA ESPERADA (ROJO): el backend consulta con un 5 fijo en vez de usar el
     * stockMinimo de cada producto. El Dashboard (frontend) sí usa stockMinimo, por eso
     * ambos pueden mostrar listas distintas.
     *
     * A: stock 8, mínimo 10  -> ES crítico   (el 5 fijo NO lo detecta)
     * B: stock 3, mínimo 2   -> NO es crítico (el 5 fijo lo marca por error)
     */
    @Test
    void productosConStockBajo_usaElStockMinimoDeCadaProducto() {
        Producto a = producto(1, 8, 10, 10, 15);
        Producto b = producto(2, 3, 2, 10, 15);
        when(productoRepository.findByActivoTrue()).thenReturn(List.of(a, b));
        // Lo que hoy hace el código (valor fijo 5):
        when(productoRepository.findByStockLessThanEqualAndActivoTrue(5)).thenReturn(List.of(b));

        List<Producto> criticos = service.productosConStockBajo();

        assertEquals(1, criticos.size());
        assertEquals(1, criticos.get(0).getId());
    }

    @Test
    void productosConStockBajo_stockIgualAlMinimoEsCritico_valorLimite() {
        Producto p = producto(1, 5, 5, 10, 15);
        when(productoRepository.findByActivoTrue()).thenReturn(List.of(p));
        when(productoRepository.findByStockLessThanEqualAndActivoTrue(5)).thenReturn(List.of(p));

        assertEquals(1, service.productosConStockBajo().size());
    }
}
