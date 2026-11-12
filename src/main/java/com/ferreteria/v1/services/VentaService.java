package com.ferreteria.v1.services;

import java.util.List;
import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ferreteria.v1.exceptions.ValidacionException;
import com.ferreteria.v1.models.Caja;
import com.ferreteria.v1.models.DetalleVenta;
import com.ferreteria.v1.models.Inventario;
import com.ferreteria.v1.models.Producto;
import com.ferreteria.v1.models.Usuario;
import com.ferreteria.v1.models.Venta;
import com.ferreteria.v1.repositories.CajaRepository;
import com.ferreteria.v1.repositories.DetalleVentaRepository;
import com.ferreteria.v1.repositories.InventarioRepository;
import com.ferreteria.v1.repositories.ProductoRepository;
import com.ferreteria.v1.repositories.VentaRepository;
import com.ferreteria.v1.repositories.ContadorComprobanteRepository;

@Service
public class VentaService {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private DetalleVentaRepository detalleVentaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private CajaRepository cajaRepository;

    @Autowired
    private ContadorComprobanteRepository contadorComprobanteRepository;

    public List<Venta> listarTodas() {
        return ventaRepository.findAll();
    }

    public Venta buscarPorId(Integer id) {
        return ventaRepository.findById(id).orElse(null);
    }

    public List<DetalleVenta> obtenerDetalle(Integer ventaId) {
        return detalleVentaRepository.findByVentaId(ventaId);
    }

    /** Anula una venta: devuelve el stock al inventario y la marca como Anulada. */
    @Transactional
    public Venta anular(Integer id, Usuario usuarioActual) {
        Venta venta = ventaRepository.buscarPorIdParaActualizar(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada id=" + id));
        if (venta.getEstado() == Venta.EstadoVenta.Anulada) {
            throw new RuntimeException("La venta ya está anulada.");
        }

        for (DetalleVenta detalle : detalleVentaRepository.findByVentaId(id)) {
            Producto producto = detalle.getProducto();
            if (producto == null) {
                continue;
            }

            Integer productoId = producto.getId();
            producto = productoRepository.buscarPorIdParaActualizar(productoId)
                    .orElse(producto);

            int stockAntes = producto.getStock() == null ? 0 : producto.getStock();
            int stockDespues = stockAntes + detalle.getCantidad();
            producto.setStock(stockDespues);
            productoRepository.save(producto);

            Inventario mov = new Inventario();
            mov.setProducto(producto);
            mov.setUsuario(usuarioActual);
            mov.setTipo(Inventario.TipoMovimiento.Entrada);
            mov.setCantidad(detalle.getCantidad());
            mov.setStockAntes(stockAntes);
            mov.setStockDespues(stockDespues);
            mov.setMotivo("Anulación venta " + venta.getNumeroComprobante());
            inventarioRepository.save(mov);
        }

        venta.setEstado(Venta.EstadoVenta.Anulada);
        return ventaRepository.save(venta);
    }

    @Transactional
    public Venta registrarVenta(Venta venta, List<DetalleVenta> detalles, Usuario usuarioActual) {
        validarVenta(venta, detalles);

        venta.setUsuario(usuarioActual);

        // Autogenera el número: serie F001 para Factura, B001 para Boleta.
        if (venta.getNumeroComprobante() == null || venta.getNumeroComprobante().isBlank()) {
            String serie = venta.getTipoComprobante() == Venta.TipoComprobante.Factura ? "F001" : "B001";
            contadorComprobanteRepository.reservarSiguiente(serie);
            Long numero = contadorComprobanteRepository.obtenerNumeroReservado();
            if (numero == null || numero <= 0) {
                throw new IllegalStateException("No se pudo reservar el numero del comprobante.");
            }

            // Evita reutilizar números cuando el contador todavía no refleja ventas existentes.
            long siguientePorVentas = ventaRepository.count() + 1;
            numero = Math.max(numero, siguientePorVentas);
            venta.setNumeroComprobante(serie + String.format("-%05d", numero));
        }

        Venta ventaGuardada = ventaRepository.save(venta);
        double subtotalTotal = 0.0;

        for (DetalleVenta detalle : detalles) {
            detalle.setVenta(ventaGuardada);

            Producto producto = productoRepository.buscarPorIdParaActualizar(
                    detalle.getProducto().getId()
            ).orElseThrow(() -> new RuntimeException(
                    "Producto no encontrado id=" + detalle.getProducto().getId()
            ));

            int stockDisponible = producto.getStock() == null ? 0 : producto.getStock();
            if (stockDisponible < detalle.getCantidad()) {
                throw new RuntimeException(
                        "Stock insuficiente para \"" + producto.getNombre() +
                        "\". Disponible: " + stockDisponible +
                        ", solicitado: " + detalle.getCantidad()
                );
            }

            double subDetalle = detalle.getCantidad() * detalle.getPrecioUnitario();
            detalle.setSubtotal(subDetalle);
            subtotalTotal += subDetalle;
            detalleVentaRepository.save(detalle);

            int stockAntes = stockDisponible;
            int stockDespues = stockAntes - detalle.getCantidad();
            producto.setStock(stockDespues);
            productoRepository.save(producto);

            Inventario mov = new Inventario();
            mov.setProducto(producto);
            mov.setUsuario(usuarioActual);
            mov.setTipo(Inventario.TipoMovimiento.Salida);
            mov.setCantidad(detalle.getCantidad());
            mov.setStockAntes(stockAntes);
            mov.setStockDespues(stockDespues);
            mov.setMotivo("Venta " + ventaGuardada.getNumeroComprobante());
            inventarioRepository.save(mov);
        }

        double igv = subtotalTotal * 0.18;
        ventaGuardada.setSubtotal(subtotalTotal);
        ventaGuardada.setIgv(igv);
        ventaGuardada.setTotal(subtotalTotal + igv);
        Venta resultado = ventaRepository.save(ventaGuardada);

        // Si hay una caja abierta, suma la venta a su total.
        cajaRepository.findFirstByEstadoOrderByAperturaDesc(Caja.Estado.Abierta)
                .ifPresent(caja -> {
                    caja.setTotalVentas(caja.getTotalVentas().add(BigDecimal.valueOf(resultado.getTotal())));
                    cajaRepository.save(caja);
                });

        return resultado;
    }

    private void validarVenta(Venta venta, List<DetalleVenta> detalles) {
        if (venta == null) {
            throw new ValidacionException("Debes proporcionar los datos de la venta.");
        }
        if (detalles == null || detalles.isEmpty()) {
            throw new ValidacionException("La venta debe contener al menos un producto.");
        }

        for (DetalleVenta detalle : detalles) {
            if (detalle == null || detalle.getProducto() == null
                    || detalle.getProducto().getId() == null) {
                throw new ValidacionException("Debes seleccionar un producto válido para cada detalle.");
            }
            if (detalle.getCantidad() == null || detalle.getCantidad() <= 0) {
                throw new ValidacionException("La cantidad vendida debe ser mayor que cero.");
            }
            if (detalle.getPrecioUnitario() == null || detalle.getPrecioUnitario() < 0) {
                throw new ValidacionException("El precio unitario no puede ser nulo ni negativo.");
            }
        }
    }
}
