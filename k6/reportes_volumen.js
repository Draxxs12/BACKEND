// HU-S4-06 · Rendimiento de reportes existentes (Libro Mayor y Auditoría de Stock).
// Ejecutar en base de pruebas con TOKEN de un administrador.
import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, cabeceras } from './comun.js';

export const options = {
  vus: Number(__ENV.VUS || 10),
  duration: __ENV.DURACION || '2m',
  thresholds: {
    checks: ['rate>0.99'],
    http_req_failed: ['rate<0.01'],
    'http_req_duration{endpoint:libro_mayor}': ['p(95)<2000'],
    'http_req_duration{endpoint:auditoria_stock}': ['p(95)<2000'],
  },
};

export default function () {
  const libro = http.get(BASE_URL + '/api/reportes/libro-mayor', {
    ...cabeceras(), tags: { endpoint: 'libro_mayor' },
  });
  check(libro, {
    'libro mayor HTTP 200': (r) => r.status === 200,
    'libro mayor contiene ventas y compras': (r) => {
      try { const body = r.json(); return Array.isArray(body.ventas) && Array.isArray(body.compras); }
      catch (_) { return false; }
    },
  });

  const stock = http.get(BASE_URL + '/api/reportes/stock', {
    ...cabeceras(), tags: { endpoint: 'auditoria_stock' },
  });
  check(stock, {
    'auditoria stock HTTP 200': (r) => r.status === 200,
    'auditoria contiene productos': (r) => {
      try { const body = r.json(); return Array.isArray(body.productos) && typeof body.capitalAlmacenado === 'number'; }
      catch (_) { return false; }
    },
  });
  sleep(1);
}
