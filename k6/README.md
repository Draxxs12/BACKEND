# Pruebas de rendimiento (Sprint 4)

Escenarios k6, mapeos WireMock y flujos de GitHub Actions del Sprint 4. Los umbrales son **propuestas**: el Product Owner debe aprobarlos antes de ejecutar. Nada de esto se ejecutó todavía contra el sistema real.

| Archivo | Para qué sirve | Historia |
|---|---|---|
| `humo.js` | Humo (3 usuarios, 30 s) en cada Pull Request | todas |
| `login_carga.js` | 30 inicios de sesión en 1 minuto | HU-S4-01 |
| `venta_pico.js` | Rampa de 5 a 100 usuarios en 10 s sobre `POST /api/ventas` | HU-S4-02 |
| `stock_concurrente.js` | 100 ventas simultáneas contra stock 50 | HU-S4-03 |
| `dashboard_volumen.js` | Lecturas que usa el dashboard con 50 000 ventas | HU-S4-04 |
| `busqueda_productos.js` | Búsqueda por nombre con 50 usuarios | HU-S4-05 |
| `estres.js` | Escalones de carga hasta el punto de quiebre | HU-S4-06, 08 |
| `resistencia.js` | Carga constante de 8 h al 40 % de la capacidad | HU-S4-07 |
| `generar_token.py` | Genera un JWT de administrador sin pasar por el correo MFA | todas |
| `../wiremock/mappings/` | Groq y Gmail lentos (30 s) o con error 500 | HU-S4-08 |

## Token (la seguridad por roles lo exige)
`SecurityConfig` restringe las rutas de negocio por rol. Los endpoints `/actuator/health/**` quedan públicos para las sondas de infraestructura; las métricas y demás endpoints de Actuator siguen protegidos. Los escenarios mezclan ventas, productos, compras y reportes, así que el token debe ser de un usuario **Administrador activo**.

El login real pide un código enviado por correo. Para las pruebas se genera el token con la misma clave del backend:

```powershell
$env:JWT_SECRET = "<la misma clave Base64 de jwt.secret del backend>"
$env:EMAIL      = "<correo de un Administrador activo de la base de pruebas>"
$env:TOKEN      = python k6\generar_token.py
```

El token dura 4 h (el backend usa 8 h). Para la resistencia de 8 h, genera uno con `$env:HORAS = 9` o renuévalo.

## Ejecutar un escenario (PowerShell, Java 17)
1. Levanta MySQL y el backend con Java 17 (`$env:JAVA_HOME` apuntando al JDK 17). `DataInitializer` crea datos de ejemplo al arrancar; para volumen usa `fase2Ferreteria.sql` u otro generador de datos de prueba.
2. Genera el token (arriba) e instala k6: `winget install k6`.
3. Ejecuta, por ejemplo:
   `k6 run -e BASE_URL=http://localhost:8080 -e PRODUCTO_ID=1 -e CLIENTE_ID=1 -e USUARIO_ID=1 k6\venta_pico.js`
4. Si un umbral falla, k6 termina con código distinto de 0: así el pipeline marca el job como fallido.

`venta_pico.js`, `estres.js` y `resistencia.js` venden el mismo producto muchas veces: antes de correrlos sube su stock (`UPDATE productos SET stock = 1000000 WHERE id = 1;`) o se agotará y los rechazos contarán como errores.

## Verificación SQL de HU-S4-03 (después de `stock_concurrente.js`)
Antes de ejecutar deja el producto con stock **exactamente 50** (`UPDATE productos SET stock = 50 WHERE id = 1;`). Después:

```sql
SELECT stock FROM productos WHERE id = 1;                                      -- debe ser 0
SELECT COUNT(*) FROM detalle_venta WHERE producto_id = 1;                      -- debe ser 50
SELECT COUNT(*) FROM inventario WHERE producto_id = 1 AND tipo = 'Salida';     -- debe ser 50
```

`VentaController` convierte **cualquier** `RuntimeException` en HTTP 400. Por eso el script cuenta como "rechazo" tanto el stock insuficiente como un posible choque de número de comprobante (`numero_comprobante` es único) o un tiempo de espera del bloqueo. Si `ventas_confirmadas` no llega a 50 aunque el stock alcanzaba, el árbitro es el SQL de arriba y el registro del backend.

## Lo que falta decidir o construir
- **Correo simulado (HU-S4-01 y 08):** `EmailService` ahora depende de `CodigoEmailSender`. En producción se usa `GmailCodigoEmailSender`; al activar `SPRING_PROFILES_ACTIVE=test` se carga `TestCodigoEmailSender`, que simula la entrega sin contactar Gmail ni registrar códigos MFA. Para ejecutar `login_carga.js`, inicia el backend con ese perfil y define `EMAIL` y `PASSWORD` de un usuario administrador de la base de pruebas. El escenario solo mide la respuesta del login; no valida la recepción real del correo.
- **Groq:** `ia.api.url` sí es configurable: apúntalo a WireMock (`http://localhost:8089/openai/v1/chat/completions`).
- **Resistencia de 8 h:** los ejecutores gratuitos de GitHub limitan cada job a 6 h; ejecútala en una máquina dedicada.
- **SonarCloud:** reemplaza `TU_ORGANIZACION` y `TU_PROYECTO_BACKEND` en `sonar-project.properties` y agrega el paso de análisis cuando tengas el `SONAR_TOKEN`.

## Secretos que necesita el flujo `rendimiento.yml`
| Secreto | Valor |
|---|---|
| `JWT_SECRET_PRUEBAS` | Clave Base64 de al menos 32 bytes, solo para CI (`openssl rand -base64 48`) |
| `EMAIL_ADMIN_PRUEBAS` | Correo de un Administrador que `DataInitializer` crea al arrancar |
