// HU-S4-07 · Resistencia (soak): carga constante equivalente al 40 % de la capacidad máxima durante 8 h.
// VUS se calcula DESPUÉS de medir la capacidad máxima con la prueba de estrés.
import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, cabeceras, ventaDeUnaUnidad } from './comun.js';

export const options = {
  vus: Number(__ENV.VUS || 20),
  duration: __ENV.DURACION || '8h',
  thresholds: {
    http_req_failed: ['rate<0.005'],                          // errores ≤ 0,5 %
    http_req_duration: ['p(95)<' + (__ENV.P95_MS || 2000)],
  },
};

export default function () {
  const r = http.post(`${BASE_URL}/api/ventas`, ventaDeUnaUnidad(), cabeceras());
  check(r, { 'venta 200': (x) => x.status === 200 });
  http.get(`${BASE_URL}/api/productos`, cabeceras());
  sleep(1);
}
