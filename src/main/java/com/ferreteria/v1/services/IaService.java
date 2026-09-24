package com.ferreteria.v1.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Análisis con IA del centro de reportes.
 * - Si hay una API key de OpenAI configurada (openai.api.key), genera el resumen con el modelo real.
 * - Si no, produce un análisis local automático a partir de los mismos datos (siempre funciona, sin costo).
 */
@Service
public class IaService {

    // Compatible con cualquier API estilo OpenAI. Por defecto: Groq (gratuito).
    @Value("${ia.api.url:https://api.groq.com/openai/v1/chat/completions}")
    private String apiUrl;

    @Value("${ia.api.key:}")
    private String apiKey;

    @Value("${ia.model:llama-3.3-70b-versatile}")
    private String model;

    private final ReporteService reporteService;
    private final ObjectMapper mapper = new ObjectMapper();

    public IaService(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    private static final String SYS_FINANCIERO =
            "Eres un analista financiero de una ferretería. Redacta en español un resumen ejecutivo "
            + "claro y breve (máximo 6 frases) con conclusiones y recomendaciones prácticas.";
    private static final String SYS_ALMACEN =
            "Eres un jefe de almacén de una ferretería. Redacta en español un diagnóstico breve "
            + "(máximo 6 frases) del inventario: capital inmovilizado, productos por reponer y recomendaciones de compra.";

    /** Análisis del Libro Mayor (finanzas). */
    public Map<String, Object> analizar(LocalDate desde, LocalDate hasta) {
        Map<String, Object> libro = reporteService.libroMayor(desde, hasta);
        Map<String, Object> stock = reporteService.auditoriaStock();
        String contexto = construirContexto(libro, stock, desde, hasta);
        return generar(SYS_FINANCIERO, contexto, () -> analisisLocal(libro, stock));
    }

    /** Análisis de la Auditoría de Almacén (inventario). */
    public Map<String, Object> analizarStock() {
        Map<String, Object> stock = reporteService.auditoriaStock();
        String contexto = "Auditoría de inventario de la ferretería 'Progresol Charito':\n"
                + "- Capital almacenado: S/ " + stock.get("capitalAlmacenado") + "\n"
                + "- Total de productos activos: " + stock.get("totalProductos") + "\n"
                + "- Productos por debajo del stock mínimo: " + contarBajoStock(stock) + "\n"
                + detalleBajoStock(stock);
        return generar(SYS_ALMACEN, contexto, () -> analisisLocalStock(stock));
    }

    /** Decide entre IA externa o análisis local. */
    private Map<String, Object> generar(String system, String contexto, java.util.function.Supplier<String> local) {
        String analisis;
        String fuente;
        if (apiKey != null && !apiKey.isBlank()) {
            try {
                analisis = llamarIA(system, contexto);
                fuente = "IA (" + model + ")";
            } catch (Exception e) {
                analisis = local.get() + "\n\n(No se pudo contactar a la IA externa; se generó un análisis local.)";
                fuente = "Análisis local";
            }
        } else {
            analisis = local.get();
            fuente = "Análisis local (sin clave de IA configurada)";
        }
        return Map.of("analisis", analisis, "fuente", fuente);
    }

    // ── Construye el texto que se le pasa a la IA ────────────────────────────
    private String construirContexto(Map<String, Object> libro, Map<String, Object> stock,
                                      LocalDate desde, LocalDate hasta) {
        return "Datos de la ferretería 'Progresol Charito' para el periodo " + desde + " a " + hasta + ":\n"
                + "- Ingresos por ventas: S/ " + libro.get("totalIngresos") + "\n"
                + "- Egresos por compras: S/ " + libro.get("totalEgresos") + "\n"
                + "- Utilidad bruta: S/ " + libro.get("utilidadBruta") + "\n"
                + "- Cuentas por pagar pendientes: S/ " + libro.get("cuentasPorPagar") + "\n"
                + "- Número de ventas en el periodo: " + tam(libro.get("ventas")) + "\n"
                + "- Capital almacenado en inventario: S/ " + stock.get("capitalAlmacenado") + "\n"
                + "- Productos en stock: " + stock.get("totalProductos") + "\n"
                + "- Productos bajo stock mínimo: " + contarBajoStock(stock) + "\n";
    }

    // ── Llamada real a la IA (Groq u otra API compatible con OpenAI) ──────────
    private String llamarIA(String system, String contexto) throws Exception {
        Map<String, Object> body = Map.of(
                "model", model,
                "temperature", 0.4,
                "messages", List.of(
                        Map.of("role", "system", "content", system),
                        Map.of("role", "user", "content", contexto)
                )
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                .build();

        HttpResponse<String> resp = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        JsonNode root = mapper.readTree(resp.body());
        return root.path("choices").get(0).path("message").path("content").asText();
    }

    // ── Análisis local automático (heurístico) ───────────────────────────────
    @SuppressWarnings("unchecked")
    private String analisisLocal(Map<String, Object> libro, Map<String, Object> stock) {
        double ingresos = num(libro.get("totalIngresos"));
        double egresos  = num(libro.get("totalEgresos"));
        double utilidad = num(libro.get("utilidadBruta"));
        double cxp      = num(libro.get("cuentasPorPagar"));
        int numVentas   = tam(libro.get("ventas"));
        double capital  = num(stock.get("capitalAlmacenado"));
        int bajoStock   = contarBajoStock(stock);

        double margen = ingresos > 0 ? (utilidad / ingresos) * 100 : 0;

        StringBuilder sb = new StringBuilder();
        sb.append("Resumen del periodo: se registraron ").append(numVentas)
          .append(" ventas por S/ ").append(fmt(ingresos)).append(" en ingresos, ")
          .append("frente a S/ ").append(fmt(egresos)).append(" en compras recibidas, ")
          .append("dejando una utilidad bruta de S/ ").append(fmt(utilidad))
          .append(" (margen aproximado del ").append(fmt(margen)).append("%).\n\n");

        if (utilidad < 0) {
            sb.append("⚠️ El periodo cerró en pérdida: los egresos superan a los ingresos. ")
              .append("Revisa precios de venta y el volumen de compras.\n");
        } else if (margen < 15) {
            sb.append("El margen es ajustado; considera revisar precios de venta o negociar mejores costos con proveedores.\n");
        } else {
            sb.append("La rentabilidad del periodo es saludable.\n");
        }

        if (cxp > 0) {
            sb.append("• Tienes S/ ").append(fmt(cxp)).append(" en cuentas por pagar pendientes a proveedores; prográmalas para no afectar la caja.\n");
        }
        if (bajoStock > 0) {
            sb.append("• Hay ").append(bajoStock).append(" producto(s) por debajo del stock mínimo: conviene generar una orden de compra pronto.\n");
        } else {
            sb.append("• El inventario está por encima de los mínimos en todos los productos.\n");
        }
        sb.append("• Capital inmovilizado en mercadería: S/ ").append(fmt(capital)).append(".");

        return sb.toString();
    }

    // ── Análisis local enfocado en inventario ────────────────────────────────
    @SuppressWarnings("unchecked")
    private String analisisLocalStock(Map<String, Object> stock) {
        double capital = num(stock.get("capitalAlmacenado"));
        int total      = (int) num(stock.get("totalProductos"));
        int bajo       = contarBajoStock(stock);

        StringBuilder sb = new StringBuilder();
        sb.append("Diagnóstico de almacén: se auditaron ").append(total)
          .append(" productos activos, con un capital inmovilizado de S/ ").append(fmt(capital)).append(".\n\n");

        if (bajo == 0) {
            sb.append("✅ Todos los productos están por encima de su stock mínimo; no hay urgencias de reposición.\n");
        } else {
            sb.append("⚠️ Hay ").append(bajo).append(" producto(s) por debajo del stock mínimo que requieren reposición:\n");
            sb.append(detalleBajoStock(stock));
            sb.append("Recomendación: genera una orden de compra para estos productos antes de que se agoten.\n");
        }
        sb.append("• Revisa los productos con mayor capital inmovilizado para evitar sobrestock.");
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private String detalleBajoStock(Map<String, Object> stock) {
        Object prods = stock.get("productos");
        if (!(prods instanceof List)) return "";
        StringBuilder sb = new StringBuilder();
        int mostrados = 0;
        for (Object o : (List<Object>) prods) {
            if (o instanceof Map) {
                Map<String, Object> p = (Map<String, Object>) o;
                if (num(p.get("stock")) <= num(p.get("stockMinimo")) && mostrados < 8) {
                    sb.append("   - ").append(p.get("nombre"))
                      .append(" (stock ").append((int) num(p.get("stock")))
                      .append(", mínimo ").append((int) num(p.get("stockMinimo"))).append(")\n");
                    mostrados++;
                }
            }
        }
        return sb.toString();
    }

    // ── Utilidades ───────────────────────────────────────────────────────────
    @SuppressWarnings("unchecked")
    private int contarBajoStock(Map<String, Object> stock) {
        Object prods = stock.get("productos");
        if (!(prods instanceof List)) return 0;
        int c = 0;
        for (Object o : (List<Object>) prods) {
            if (o instanceof Map) {
                Map<String, Object> p = (Map<String, Object>) o;
                if (num(p.get("stock")) <= num(p.get("stockMinimo"))) c++;
            }
        }
        return c;
    }

    private int tam(Object lista) { return (lista instanceof List) ? ((List<?>) lista).size() : 0; }
    private double num(Object o)  { return (o instanceof Number) ? ((Number) o).doubleValue() : 0.0; }
    private String fmt(double v)  { return String.format("%.2f", v); }
}
