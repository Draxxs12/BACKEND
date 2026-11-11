// HU-S4-02 · Pico: rampa de 5 a 100 usuarios virtuales en 10 s, se mantiene 2 min y vuelve a 5.
// Usar un producto con stock alto y datos de prueba aislados.
import http from 'k6/http';
import { check } from 'k6';
import { BASE_URL, cabeceras, ventaDeUnaUnidad } from './comun.js';

export const options = {
  stages: [
    { duration: '30s', target: 5 },     // línea base
    { duration: '10s', target: 100 },   // pico
    { duration: '2m',  target: 100 },
    { duration: '10s', target: 5 },     // regreso
    { duration: '60s', target: 5 },     // ventana de recuperación (≤ 60 s)
  ],
  thresholds: {
    'http_req_duration{endpoint:venta}': ['p(95)<2000'],   // criterio 1
    http_req_failed: ['rate<0.02'],
  },
};

export default function () {
  const r = http.post(`${BASE_URL}/api/ventas`, ventaDeUnaUnidad(), { ...cabeceras(), tags: { endpoint: 'venta' } });
  check(r, { 'venta 200': (x) => x.status === 200 });
}
