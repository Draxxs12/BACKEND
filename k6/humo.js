// Prueba de humo para cada Pull Request: 3 usuarios virtuales durante 30 s.
import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, cabeceras } from './comun.js';

export const options = {
  vus: 3,
  duration: '30s',
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<1500'],
  },
};

export default function () {
  // SecurityConfig exige autenticación también para /actuator/**, por eso se envía el token.
  const salud = http.get(`${BASE_URL}/actuator/health`, cabeceras());
  check(salud, { 'health 200': (r) => r.status === 200 });

  const productos = http.get(`${BASE_URL}/api/productos`, cabeceras());
  check(productos, { 'productos 200': (r) => r.status === 200 });
  sleep(1);
}
