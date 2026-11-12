# Reglas de calidad estática y seguridad – Backend

Este documento describe las reglas que se ejecutan o se proponen para el repositorio BACKEND. No sustituye los resultados de ejecución: los reportes de GitHub Actions son la evidencia.

## 1. Reglas configuradas en el repositorio

| Herramienta | Regla aplicada | Comportamiento |
|---|---|---|
| SpotBugs | effort=Max; threshold=High | Analiza bytecode con el esfuerzo máximo y reporta defectos de prioridad alta. El goal spotbugs:check hace fallar el job cuando detecta hallazgos que superan el umbral. |
| FindSecBugs | Plugin 1.13.0 integrado en SpotBugs | Activa detectores de seguridad sobre el bytecode Java: criptografía débil o mal utilizada, validación de entradas, inyección y problemas de seguridad en APIs. |
| OWASP Dependency-Check | failBuildOnCVSS=9 | Genera inventario de dependencias y falla cuando identifica vulnerabilidades con CVSS igual o superior a 9.0. |
| Gitleaks | Escaneo del historial completo (fetch-depth: 0) | Falla el job si detecta secretos reconocibles en el repositorio o historial. No publicar los valores encontrados en reportes. |
| JaCoCo | Reporte durante verify | Genera cobertura XML/HTML para medir cobertura; actualmente no impone un mínimo automático del 70 %. |

## 2. Categorías revisadas por SpotBugs / FindSecBugs

- Correctitud: errores de lógica, condiciones incorrectas y posibles excepciones.
- Fiabilidad: uso incorrecto de recursos y operaciones que pueden fallar.
- Rendimiento: operaciones costosas o asignaciones innecesarias detectables estáticamente.
- Concurrencia: accesos compartidos y sincronización incorrecta detectables por los analizadores.
- Seguridad: criptografía insegura, validación insuficiente, inyección y uso inseguro de APIs.

La configuración actual usa threshold High; por tanto, no se debe afirmar que bloquea todos los hallazgos de prioridad baja o media.

## 3. Quality Gate propuesto para SonarCloud

SonarCloud todavía requiere crear/vincular el proyecto y configurar SONAR_TOKEN, SONAR_PROJECT_KEY y SONAR_ORGANIZATION en GitHub. Hasta que esas variables existan y el análisis se ejecute, SonarCloud no se considera activo ni aprobado.

Reglas recomendadas para código nuevo:
- Bugs nuevos: 0 bloqueantes.
- Vulnerabilidades nuevas: 0 críticas y 0 altas.
- Security Hotspots: 100 % revisados.
- Cobertura de código nuevo: al menos 70 %.
- Código duplicado nuevo: máximo 3 %.
- Reliability Rating: A.
- Security Rating: A.

Estas reglas son una propuesta de Quality Gate; deben configurarse en SonarCloud y aprobarse con el Product Owner. No se presentan como una configuración ya aplicada.

## 4. Ejecución local

Desde la raíz del backend, con Java 17 y Maven Wrapper:

```powershell
.mvnw.cmd -B verify
.mvnw.cmd -B spotbugs:check
.mvnw.cmd -B org.owasp:dependency-check-maven:10.0.4:check
```

Reportes esperados:
- JaCoCo: target/site/jacoco/index.html
- SpotBugs: target/spotbugsXml.xml
- OWASP Dependency-Check: target/dependency-check-report.html

No subir reportes que contengan secretos ni copiar credenciales a incidencias. Para evidencias académicas, capturar el resumen del job y los conteos/severidades, ocultando cualquier valor sensible.
