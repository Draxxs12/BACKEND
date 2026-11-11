// HU-S4-04 · Carga y volumen: el dashboard del frontend obtiene sus datos con estas lecturas.
// Ejecutar con la base de prueba de 50 000 ventas y 5 000 productos; 10 usuarios simultáneos.
import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, cabeceras } from './comun.js';

export const options = {
  vus: 10,
  duration: '2m',
  thresholds: {
    'http_req_duration{endpoint:ventas}': ['p(95)<1500'],
    'http_req_duration{endpoint:productos}': ['p(95)<1500'],
    http_req_failed: ['rate<0.01'],
  },
};

export default function () {
  const g = http.batch([
    ['GET', `${BASE_URL}/api/ventas`, null, { ...cabeceras(), tags: { endpoint: 'ventas' } }],
    ['GET', `${BASE_URL}/api/productos`, null, { ...cabeceras(), tags: { endpoint: 'productos' } }],
    ['GET', `${BASE_URL}/api/compras`, null, { ...cabeceras(), tags: { endpoint: 'compras' } }],
  ]);
  g.forEach((r) => check(r, { '200': (x) => x.status === 200 }));
  sleep(2);
}
