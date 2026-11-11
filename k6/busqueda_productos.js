// HU-S4-05 · Carga y volumen: búsqueda por nombre con 50 usuarios simultáneos y 20 000 productos.
import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, cabeceras } from './comun.js';

export const options = {
  vus: 50,
  duration: '2m',
  thresholds: {
    'http_req_duration{endpoint:buscar}': ['p(95)<500'],   // criterio 1
    http_req_failed: ['rate<0.01'],                        // criterio 2
  },
};

const TEXTOS = ['mar', 'tal', 'cem', 'cla', 'pin', 'tub'];

export default function () {
  const texto = TEXTOS[Math.floor(Math.random() * TEXTOS.length)];
  const r = http.get(`${BASE_URL}/api/productos/buscar?nombre=${texto}`, { ...cabeceras(), tags: { endpoint: 'buscar' } });
  check(r, { 'buscar 200': (x) => x.status === 200 });
  sleep(0.5);
}
