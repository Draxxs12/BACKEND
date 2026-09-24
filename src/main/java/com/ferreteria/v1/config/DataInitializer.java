package com.ferreteria.v1.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.ferreteria.v1.models.*;
import com.ferreteria.v1.repositories.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Siembra datos al arrancar: rellena "registrado_por" donde falte y garantiza
 * al menos ~20 registros en cada sección (usuarios, clientes, proveedores,
 * ventas, compras, devoluciones, caja). Todo es idempotente: no duplica.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired private RolRepository              rolRepo;
    @Autowired private UsuarioRepository          usuarioRepo;
    @Autowired private CategoriaRepository        categoriaRepo;
    @Autowired private ProductoRepository         productoRepo;
    @Autowired private ClienteRepository          clienteRepo;
    @Autowired private ProveedorRepository        proveedorRepo;
    @Autowired private VentaRepository            ventaRepo;
    @Autowired private DetalleVentaRepository      detalleVentaRepo;
    @Autowired private InventarioRepository        inventarioRepo;
    @Autowired private CompraRepository            compraRepo;
    @Autowired private DetalleCompraRepository      detalleCompraRepo;
    @Autowired private DevolucionRepository         devolucionRepo;
    @Autowired private DetalleDevolucionRepository  detalleDevolucionRepo;
    @Autowired private CajaRepository               cajaRepo;
    @Autowired private MovimientoCajaRepository      movimientoCajaRepo;
    @Autowired private PasswordEncoder              encoder;

    private static final int META = 20;   // mínimo de registros por sección
    private static final String[] NOMBRES = {
        "Juan Pérez","María López","Carlos Díaz","Ana Ramírez","Luis Torres","Rosa Flores",
        "Pedro Castro","Lucía Vega","Miguel Soto","Elena Ríos","Jorge Mendoza","Sofía Ruiz",
        "Diego Ramos","Carmen Silva","Raúl Chávez","Patricia León","Andrés Rojas","Gloria Núñez",
        "Fernando Cruz","Beatriz Aguilar","Óscar Herrera","Natalia Paredes","Víctor Salas","Diana Campos"
    };
    private static final String[] EMPRESAS = {
        "Distribuidora Ferretera SAC","Herramientas Pro EIRL","Aceros del Perú SAC","Pinturas Tricolor EIRL",
        "Electro Suministros SAC","Cemento Andino Distrib.","Tuberías del Sur SAC","Maderas y Más EIRL",
        "Import Tools Perú SAC","Ferrecentro Mayorista","Grupo Construye SAC","Suministros Andinos EIRL",
        "Comercial El Perno SAC","Metales Unidos EIRL","PVC Total SAC","Iluminación LED Perú",
        "Abrasivos Industriales SAC","Adhesivos y Selladores EIRL","Seguridad Laboral SAC","Bombas y Motores EIRL"
    };

    @Override
    public void run(String... args) {
        rellenarRegistradoPor();
        sembrarRoles();
        sembrarBase();          // usuarios, categorías, productos, clientes, proveedores base
        completarUsuarios();
        completarCategorias();
        completarProductosPorCategoria();   // al menos 5 productos por categoría
        completarClientes();
        completarProveedores();

        List<Producto> prods    = productoRepo.findByActivoTrue();
        List<Cliente>  clientes = clienteRepo.findAll();
        List<Usuario>  users    = usuarioRepo.findAll();

        completarVentas(prods, clientes, users);
        completarCompras(prods, users);
        completarDevoluciones(users, prods);
        completarCaja(users);
    }

    // ===== Rellena "registrado_por" donde esté vacío =====
    private void rellenarRegistradoPor() {
        for (Usuario u : usuarioRepo.findAll())    if (u.getRegistradoPor() == null) { u.setRegistradoPor(u.getEmail().equals("admin@tienda.com") ? "Sistema" : "Ana Torres"); usuarioRepo.save(u); }
        for (Categoria c : categoriaRepo.findAll()) if (c.getRegistradoPor() == null) { c.setRegistradoPor("Ana Torres"); categoriaRepo.save(c); }
        for (Producto p : productoRepo.findAll())   if (p.getRegistradoPor() == null) { p.setRegistradoPor("Carlos Ruiz"); productoRepo.save(p); }
        for (Cliente cl : clienteRepo.findAll())    if (cl.getRegistradoPor() == null) { cl.setRegistradoPor("Luis Mendoza"); clienteRepo.save(cl); }
        for (Proveedor pr : proveedorRepo.findAll()) if (pr.getRegistradoPor() == null) { pr.setRegistradoPor("Ana Torres"); proveedorRepo.save(pr); }
    }

    private void sembrarRoles() {
        if (rolRepo.count() == 0) {
            for (String n : List.of("Administrador", "Recepcionista", "Almacenero")) {
                Rol r = new Rol(); r.setNombre(n); rolRepo.save(r);
            }
        }
    }

    // ===== Usuarios / categorías / productos / clientes / proveedores base =====
    private void sembrarBase() {
        Rol admin = rolRepo.findByNombre("Administrador").orElse(null);
        Rol recep = rolRepo.findByNombre("Recepcionista").orElse(null);
        Rol almac = rolRepo.findByNombre("Almacenero").orElse(null);
        crearUsuario("Ana Torres",      "admin@tienda.com",     admin, "Sistema");
        crearUsuario("Luis Mendoza",    "recepcion@tienda.com", recep, "Ana Torres");
        crearUsuario("Carlos Ruiz",     "almacen@tienda.com",   almac, "Ana Torres");
        crearUsuario("Sofía Ramírez",   "sofia@tienda.com",     recep, "Ana Torres");
        crearUsuario("Pedro Castillo",  "pedro@tienda.com",     almac, "Ana Torres");
        crearUsuario("Lucía Fernández", "lucia@tienda.com",     admin, "Ana Torres");

        for (String n : List.of("Herramientas Manuales", "Herramientas Eléctricas",
                                 "Plomería", "Electricidad", "Ferretería General")) {
            crearCategoria(n);
        }

        List<Categoria> cats = categoriaRepo.findAll();
        Categoria c0 = cats.size() > 0 ? cats.get(0) : null;
        Categoria c1 = cats.size() > 1 ? cats.get(1) : c0;
        Categoria c2 = cats.size() > 2 ? cats.get(2) : c0;
        Categoria c3 = cats.size() > 3 ? cats.get(3) : c0;
        Categoria c4 = cats.size() > 4 ? cats.get(4) : c0;
        crearProducto("PROD-001","Martillo Carpintero 16oz",  c0,18.0,35.0,50,10);
        crearProducto("PROD-002","Destornillador Estrella #2", c0, 5.0,12.0,80,15);
        crearProducto("PROD-003","Taladro Percutor 500W",      c1,85.0,159.9,20,5);
        crearProducto("PROD-004","Sierra Circular 7-1/4\"",    c1,120.0,229.0,10,3);
        crearProducto("PROD-005","Llave Francesa 10\"",        c0,12.0,24.0,40,10);
        crearProducto("PROD-006","Tubo PVC 1/2\" x 3m",        c2,4.5,9.0,100,20);
        crearProducto("PROD-007","Cable THW 2.5mm (metro)",    c3,1.8,3.5,500,50);
        crearProducto("PROD-008","Caja de Clavos 2\" (kg)",    c4,4.0,8.0,60,10);
        crearProducto("PROD-009","Cinta Métrica 5m",           c0,7.0,14.0,35,10);
        crearProducto("PROD-010","Nivel de Burbuja 60cm",      c0,15.0,29.0,25,5);
        crearProducto("PROD-011","Alicate Universal 8\"",      c0,9.0,19.0,45,10);
        crearProducto("PROD-012","Juego de Llaves Allen",      c0,8.0,16.0,30,8);
        crearProducto("PROD-013","Amoladora Angular 820W",     c1,95.0,179.0,15,4);
        crearProducto("PROD-014","Lijadora Orbital 240W",      c1,70.0,135.0,12,4);
        crearProducto("PROD-015","Caño PVC 1\" x 3m",          c2,6.0,12.0,80,20);
        crearProducto("PROD-016","Llave de Paso 1/2\"",        c2,8.0,16.5,50,12);
        crearProducto("PROD-017","Teflón 3/4\" (rollo)",       c2,1.0,2.5,200,40);
        crearProducto("PROD-018","Interruptor Simple",         c3,3.0,7.0,120,25);
        crearProducto("PROD-019","Tomacorriente Doble",        c3,4.5,9.5,100,25);
        crearProducto("PROD-020","Foco LED 9W",                c3,5.0,11.0,150,30);
        crearProducto("PROD-021","Cinta Aislante (unid)",      c3,1.2,3.0,300,50);
        crearProducto("PROD-022","Candado 40mm",               c4,9.0,18.0,60,12);
        crearProducto("PROD-023","Brocha 3\"",                 c4,4.0,8.5,90,20);
        crearProducto("PROD-024","Silicona Transparente",      c4,6.0,12.5,70,15);
        crearProducto("PROD-025","Disco de Corte Metal 7\"",   c1,3.5,7.5,110,25);

        crearCliente("Cliente General", Cliente.TipoDocumento.DNI, "00000000",   "",          "");
        crearCliente("María García",    Cliente.TipoDocumento.DNI, "45678901",   "987654321", "maria@gmail.com");
        crearCliente("Empresa ABC SAC", Cliente.TipoDocumento.RUC, "20987654321","015550000", "compras@abc.com");
    }

    // ===== Completar hasta META =====
    private void completarUsuarios() {
        List<Rol> roles = rolRepo.findAll();
        int i = 1;
        while (usuarioRepo.count() < META) {
            String email = "usuario" + i + "@tienda.com";
            if (usuarioRepo.findByEmail(email).isEmpty()) {
                Rol rol = roles.get(i % roles.size());
                crearUsuario(NOMBRES[i % NOMBRES.length], email, rol, "Ana Torres");
            }
            i++;
            if (i > 200) break;
        }
    }

    private void completarCategorias() {
        String[] extra = {"Cerrajería","Pinturas","Adhesivos","Seguridad","Iluminación",
                          "Jardinería","Gasfitería","Soldadura","Abrasivos","Fijaciones","Medición","Limpieza",
                          "Tornillería","Plásticos","Accesorios","Automotriz","Construcción"};
        int i = 0;
        while (categoriaRepo.count() < META && i < extra.length) { crearCategoria(extra[i]); i++; }
    }

    /** Garantiza al menos 5 productos en cada categoría (crea genéricos si faltan). */
    private void completarProductosPorCategoria() {
        List<Categoria> cats = categoriaRepo.findAll();
        List<Producto> todos = productoRepo.findAll();
        for (Categoria cat : cats) {
            long n = todos.stream()
                    .filter(p -> p.getCategoria() != null && p.getCategoria().getId().equals(cat.getId()))
                    .count();
            int idx = 1;
            while (n < 5) {
                String codigo = "C" + cat.getId() + "-" + String.format("%03d", idx);
                if (productoRepo.findByCodigo(codigo).isEmpty()) {
                    double compra = 6.0 + idx * 3.0;
                    double venta  = redondear(compra * 1.6);
                    crearProducto(codigo, cat.getNombre() + " - Artículo " + idx, cat,
                            compra, venta, 30 + idx * 5, 5);
                    n++;
                }
                idx++;
                if (idx > 50) break;
            }
        }
    }

    private void completarClientes() {
        int i = 1;
        while (clienteRepo.count() < META) {
            boolean esRuc = (i % 4 == 0);
            String doc = esRuc ? String.format("%020d", 20000000000L + i) : String.format("%08d", 40000000 + i);
            if (clienteRepo.findByNumeroDocumento(doc).isEmpty()) {
                crearCliente(NOMBRES[i % NOMBRES.length] + " " + i,
                        esRuc ? Cliente.TipoDocumento.RUC : Cliente.TipoDocumento.DNI,
                        doc, "9" + String.format("%08d", 80000000 + i),
                        "cliente" + i + "@correo.com");
            }
            i++;
            if (i > 200) break;
        }
    }

    private void completarProveedores() {
        int i = 0;
        while (proveedorRepo.count() < META && i < EMPRESAS.length) {
            if (proveedorRepo.findByEmpresaContainingIgnoreCase(EMPRESAS[i]).isEmpty()) {
                Proveedor p = new Proveedor();
                p.setEmpresa(EMPRESAS[i]);
                p.setRuc(String.format("%011d", 20500000000L + i));
                p.setContacto(NOMBRES[i % NOMBRES.length]);
                p.setTelefono("01-" + (200 + i) + "-" + (3000 + i));
                p.setEmail("ventas" + i + "@" + EMPRESAS[i].replaceAll("[^A-Za-z]", "").toLowerCase() + ".com");
                p.setDireccion("Av. Industrial " + (100 + i * 7) + ", Lima");
                p.setActivo(true);
                p.setRegistradoPor("Ana Torres");
                proveedorRepo.save(p);
            }
            i++;
        }
    }

    private void completarVentas(List<Producto> prods, List<Cliente> clientes, List<Usuario> users) {
        if (prods.isEmpty() || clientes.isEmpty() || users.isEmpty()) return;
        Venta.TipoPago[] pagos = Venta.TipoPago.values();
        int i = 1;
        while (ventaRepo.count() < META) {
            boolean esFactura = (i % 3 == 0);   // 1 de cada 3 es factura
            String num = String.format((esFactura ? "F900-%05d" : "B900-%05d"), i);
            if (ventaRepo.findByEstado(Venta.EstadoVenta.Completada).stream()
                    .noneMatch(v -> num.equals(v.getNumeroComprobante()))
                && ventaRepo.findByEstado(Venta.EstadoVenta.Anulada).stream()
                    .noneMatch(v -> num.equals(v.getNumeroComprobante()))) {
                Usuario u = users.get(i % users.size());
                Cliente cliente = clientes.get(i % clientes.size());
                Venta v = new Venta();
                v.setNumeroComprobante(num);
                v.setCliente(cliente);
                v.setUsuario(u);
                v.setTipoPago(pagos[i % pagos.length]);
                v.setTipoComprobante(esFactura ? Venta.TipoComprobante.Factura : Venta.TipoComprobante.Boleta);
                v.setRucCliente(cliente.getNumeroDocumento());
                v.setEstado(i % 8 == 0 ? Venta.EstadoVenta.Anulada : Venta.EstadoVenta.Completada);
                v.setCreatedAt(LocalDateTime.now().minusDays(i % 14).minusHours(i % 9));
                Venta saved = ventaRepo.save(v);

                double sub = 0;
                int lineas = 1 + (i % 3);
                for (int l = 0; l < lineas; l++) {
                    Producto p = prods.get((i + l) % prods.size());
                    int cant = 1 + ((i + l) % 4);
                    double precio = p.getPrecioVenta() != null ? p.getPrecioVenta() : 0.0;
                    double linea = precio * cant;
                    DetalleVenta d = new DetalleVenta();
                    d.setVenta(saved); d.setProducto(p); d.setCantidad(cant);
                    d.setPrecioUnitario(precio); d.setSubtotal(linea);
                    detalleVentaRepo.save(d);
                    sub += linea;

                    Inventario mov = new Inventario();
                    mov.setProducto(p); mov.setUsuario(u);
                    mov.setTipo(Inventario.TipoMovimiento.Salida); mov.setCantidad(cant);
                    mov.setStockAntes(p.getStock()); mov.setStockDespues(Math.max(0, p.getStock() - cant));
                    mov.setMotivo("Venta " + num);
                    inventarioRepo.save(mov);
                }
                double igv = redondear(sub * 0.18);
                saved.setSubtotal(redondear(sub)); saved.setIgv(igv); saved.setTotal(redondear(sub + igv));
                ventaRepo.save(saved);
            }
            i++;
            if (i > 300) break;
        }
    }

    private void completarCompras(List<Producto> prods, List<Usuario> users) {
        List<Proveedor> provs = proveedorRepo.findAll();
        if (prods.isEmpty() || provs.isEmpty() || users.isEmpty()) return;
        Compra.TipoPago[] pagos = Compra.TipoPago.values();
        Compra.Estado[] estados = Compra.Estado.values();
        int i = 1;
        while (compraRepo.count() < META) {
            Compra c = new Compra();
            c.setNumeroOrden(String.format("OC-9%04d", i));
            c.setProveedor(provs.get(i % provs.size()));
            c.setUsuario(users.get(i % users.size()));
            c.setTipoPago(pagos[i % pagos.length]);
            c.setEstado(estados[i % estados.length]);
            c.setFechaEsperada(LocalDate.now().plusDays(i % 10));
            c.setObservaciones("Reposición automática #" + i);
            c.setCreatedAt(LocalDateTime.now().minusDays(i % 20));
            Compra saved = compraRepo.save(c);

            BigDecimal sub = BigDecimal.ZERO;
            int lineas = 1 + (i % 3);
            for (int l = 0; l < lineas; l++) {
                Producto p = prods.get((i * 2 + l) % prods.size());
                int cant = 5 + ((i + l) % 6);
                BigDecimal precio = BigDecimal.valueOf(p.getPrecioCompra() != null ? p.getPrecioCompra() : 0.0);
                BigDecimal linea = precio.multiply(BigDecimal.valueOf(cant));
                DetalleCompra d = new DetalleCompra();
                d.setCompra(saved); d.setProducto(p); d.setCantidad(cant);
                d.setPrecioUnitario(precio); d.setSubtotal(linea);
                detalleCompraRepo.save(d);
                sub = sub.add(linea);
            }
            BigDecimal igv = sub.multiply(new BigDecimal("0.18"));
            saved.setSubtotal(sub); saved.setIgv(igv); saved.setTotal(sub.add(igv));
            compraRepo.save(saved);
            i++;
            if (i > 300) break;
        }
    }

    private void completarDevoluciones(List<Usuario> users, List<Producto> prods) {
        List<Venta> ventas = ventaRepo.findByEstado(Venta.EstadoVenta.Completada);
        if (ventas.isEmpty() || users.isEmpty() || prods.isEmpty()) return;
        Devolucion.TipoReembolso[] tipos = Devolucion.TipoReembolso.values();
        String[] motivos = {"Producto defectuoso","Cambio de medida","Cliente se arrepintió",
                            "Error en el pedido","Producto equivocado","Garantía"};
        int i = 1;
        while (devolucionRepo.count() < META) {
            Venta venta = ventas.get(i % ventas.size());
            Usuario u = users.get(i % users.size());
            Producto p = prods.get(i % prods.size());
            int cant = 1 + (i % 2);
            BigDecimal precio = BigDecimal.valueOf(p.getPrecioVenta() != null ? p.getPrecioVenta() : 0.0);
            BigDecimal linea = precio.multiply(BigDecimal.valueOf(cant));

            Devolucion dev = new Devolucion();
            dev.setNumeroNota(String.format("NC-9%04d", i));
            dev.setVenta(venta);
            dev.setUsuario(u);
            dev.setMotivo(motivos[i % motivos.length]);
            dev.setTipoReembolso(tipos[i % tipos.length]);
            dev.setMontoReembolso(linea);
            dev.setCreatedAt(LocalDateTime.now().minusDays(i % 12));
            Devolucion saved = devolucionRepo.save(dev);

            DetalleDevolucion d = new DetalleDevolucion();
            d.setDevolucion(saved); d.setProducto(p); d.setCantidad(cant);
            d.setPrecioUnitario(precio); d.setSubtotal(linea);
            detalleDevolucionRepo.save(d);
            i++;
            if (i > 300) break;
        }
    }

    private void completarCaja(List<Usuario> users) {
        if (users.isEmpty()) return;
        int i = 1;
        while (cajaRepo.count() < META) {
            Caja caja = new Caja();
            caja.setUsuario(users.get(i % users.size()));
            caja.setMontoInicial(new BigDecimal("150.00"));
            BigDecimal ventas = new BigDecimal(300 + (i * 37) % 800);
            BigDecimal egresos = new BigDecimal(20 + (i * 7) % 90);
            caja.setTotalVentas(ventas);
            caja.setTotalEgresos(egresos);
            caja.setMontoFinal(new BigDecimal("150.00").add(ventas).subtract(egresos));
            caja.setEstado(Caja.Estado.Cerrada);
            caja.setObservaciones("Turno #" + i);
            caja.setApertura(LocalDateTime.now().minusDays(i).withHour(8).withMinute(0));
            caja.setCierre(LocalDateTime.now().minusDays(i).withHour(18).withMinute(0));
            Caja savedCaja = cajaRepo.save(caja);

            MovimientoCaja m = new MovimientoCaja();
            m.setCaja(savedCaja); m.setTipo(MovimientoCaja.Tipo.Egreso);
            m.setMonto(egresos); m.setDescripcion("Gasto operativo del turno #" + i);
            movimientoCajaRepo.save(m);
            i++;
            if (i > 300) break;
        }
    }

    // ===== Helpers de creación (idempotentes) =====
    private void crearUsuario(String nombre, String email, Rol rol, String registradoPor) {
        if (usuarioRepo.findByEmail(email).isPresent()) return;
        Usuario u = new Usuario();
        u.setNombre(nombre); u.setEmail(email);
        u.setPasswordHash(encoder.encode("admin123"));
        u.setRol(rol); u.setActivo(true);
        u.setRegistradoPor(registradoPor);
        usuarioRepo.save(u);
    }

    private void crearCategoria(String nombre) {
        boolean existe = categoriaRepo.findAll().stream().anyMatch(c -> nombre.equals(c.getNombre()));
        if (existe) return;
        Categoria c = new Categoria();
        c.setNombre(nombre);
        c.setRegistradoPor("Ana Torres");
        categoriaRepo.save(c);
    }

    private void crearProducto(String codigo, String nombre, Categoria cat,
                               double compra, double venta, int stock, int min) {
        if (productoRepo.findByCodigo(codigo).isPresent()) return;
        Producto p = new Producto();
        p.setCodigo(codigo); p.setNombre(nombre); p.setCategoria(cat);
        p.setPrecioCompra(compra); p.setPrecioVenta(venta);
        p.setStock(stock); p.setStockMinimo(min); p.setActivo(true);
        p.setRegistradoPor("Carlos Ruiz");
        productoRepo.save(p);
    }

    private void crearCliente(String nombre, Cliente.TipoDocumento tipo, String doc, String tel, String email) {
        if (doc != null && !doc.isBlank() && clienteRepo.findByNumeroDocumento(doc).isPresent()) return;
        Cliente c = new Cliente();
        c.setNombre(nombre); c.setTipoDocumento(tipo); c.setNumeroDocumento(doc);
        c.setTelefono(tel); c.setEmail(email);
        c.setRegistradoPor("Luis Mendoza");
        clienteRepo.save(c);
    }

    private double redondear(double v) { return Math.round(v * 100.0) / 100.0; }
}
