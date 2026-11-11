// HU-S4-01 · Carga: 30 inicios de sesión en 1 minuto.
// Requiere un perfil de pruebas donde el correo esté simulado (EmailService falso), para no medir ni saturar Gmail.
import http from 'k6/http';
import { check } from 'k6';
import { BASE_URL } from './comun.js';

export const options = {
  scenarios: {
    apertura_de_tienda: {
      executor: 'constant-arrival-rate',
      rate: 30, timeUnit: '1m', duration: '1m',
      preAllocatedVUs: 10, maxVUs: 30,
    },
  },
  thresholds: {
    'http_req_duration{endpoint:login}': ['p(95)<1000'],   // criterio 1
    http_req_failed: ['rate<0.01'],                        // criterio 2
  },
};

export default function () {
  const r = http.post(
    `${BASE_URL}/api/auth/login`,
    JSON.stringify({ email: __ENV.EMAIL, password: __ENV.PASSWORD }),
    { headers: { 'Content-Type': 'application/json' }, tags: { endpoint: 'login' } },
  );
  check(r, { 'login 200 con challengeToken': (x) => x.status === 200 && x.json('challengeToken') !== undefined });
}
