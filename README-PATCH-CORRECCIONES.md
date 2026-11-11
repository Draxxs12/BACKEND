# Correcciones técnicas incluidas

Este patch se construyó sobre la copia de trabajo del backend recibida en BACKEND.zip.
No modifica AuthController, EmailService ni el flujo OAuth de Google/Gmail.

## Cambios

1. Bloqueo pesimista de productos durante ventas, anulaciones y recepción de compras.
2. Numeración de comprobantes por serie mediante contador transaccional MySQL.
3. Registro de movimientos de entrada al recibir compras.
4. Consultas del libro mayor filtradas en base de datos por rango de fechas.
5. Timeout y validación de respuestas para la llamada externa de IA.
6. Actuator y Prometheus para métricas.
7. Desactivación del SQL completo en logs y nuevo .gitignore.
8. Ajuste de stubs de pruebas para los nuevos métodos de repositorio.

## Consideraciones

- Requiere MySQL para la reserva atómica del número de comprobante.
- Hibernate crea la tabla contador_comprobante mediante ddl-auto=update.
- Revisar reglas de Spring Security antes de publicar endpoints Actuator.
- .gitignore no elimina archivos que ya estén versionados.
- Respaldar la base de datos antes de probar en un entorno con datos.
- No se incluyen cambios de credenciales ni del flujo OAuth de Google.
- Este patch no ejecuta pruebas de carga ni acredita resultados de rendimiento.

## Aplicación (PowerShell)

    git status
    git apply --check .\correcciones-ferreteria.patch
    git apply .\correcciones-ferreteria.patch
    .\mvnw.cmd clean test
