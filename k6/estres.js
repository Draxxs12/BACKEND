// Estrés: sube la carga por escalones hasta encontrar el punto de quiebre y comprueba la recuperación.
import http from 'k6/http';
import { check } from 'k6';
import { BASE_URL, cabeceras, ventaDeUnaUnidad } from './comun.js';

export const options = {
  stages: [
    { duration: '2m', target: 25 }, { duration: '2m', target: 50 },
    { duration: '2m', target: 100 }, { duration: '2m', target: 150 },
    { duration: '2m', target: 200 }, { duration: '3m', target: 5 },   // recuperación
  ],
  thresholds: { http_req_failed: [{ threshold: 'rate<0.10', abortOnFail: false }] },
};

export default function () {
  const r = http.post(`${BASE_URL}/api/ventas`, ventaDeUnaUnidad(), cabeceras());
  check(r, { 'sin 5xx': (x) => x.status < 500 });
}
