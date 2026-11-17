# Migración a Spring Boot 4.1.2 y Java 25

## Alcance
Actualizar el backend manteniendo los contratos REST y las funciones existentes: autenticación JWT/MFA, roles, usuarios, productos, categorías, inventario, compras, ventas, devoluciones, caja, clientes, proveedores, reportes, Gmail, Groq y Actuator.

## Punto de partida
- Spring Boot 3.3.5.
- Java 17, validado por Maven Enforcer.
- Spring Security con `DaoAuthenticationProvider`.
- Jackson 2 utilizado directamente por `IaService`.
- MySQL mediante Spring Data JPA.
- CI con Temurin 17 y pruebas MySQL.
- Workflow de rendimiento separado.

## Objetivo
- Spring Boot 4.1.2 (versión estable, no una RC).
- Java 25 LTS.
- Spring Framework 7 y Spring Security 7 gestionados por el BOM de Spring Boot.
- Jackson 3.
- Mantener MySQL, JWT, MFA, Gmail, Groq, endpoints y reglas de autorización.

## Cambios iniciados en esta rama
- Parent Spring Boot actualizado a 4.1.2.
- Propiedad Java y Maven Enforcer actualizados a 25.
- Starter MVC cambiado a `spring-boot-starter-webmvc`.
- Proveedor de autenticación adaptado al constructor de Spring Security 7.
- `IaService` migrado a `tools.jackson` / Jackson 3.
- Workflow de calidad actualizado a Temurin 25.

## Validaciones obligatorias antes de considerar terminada la migración
1. `./mvnw -B verify` con MySQL.
2. `./mvnw -B spotbugs:check`.
3. Workflow de rendimiento: comprobar que usa Java 25 y que el backend arranca.
4. Pruebas de login, MFA, JWT, autorización por roles y recuperación de contraseña.
5. CRUD de los módulos operativos y reglas de concurrencia de stock, compras y devoluciones.
6. Gmail y Groq con credenciales configuradas; fallback local de IA sin clave.
7. Actuator, métricas Prometheus y configuración de base de datos.
8. Revisar las dependencias de terceros y OWASP Dependency-Check.

## Criterio de aceptación
No se declara compatible hasta que las pruebas automatizadas y las comprobaciones funcionales anteriores terminen correctamente. No se modificará ni fusionará `main` desde esta rama.
