// HU-S4-03 · Concurrencia: 100 solicitudes simultáneas de 1 unidad contra un producto con stock 50.
// Antes de ejecutar: dejar PRODUCTO_ID con stock exactamente 50. Después: verificar con SQL (ver README).
import http from 'k6/http';
import { Counter } from 'k6/metrics';
import { BASE_URL, cabeceras, ventaDeUnaUnidad } from './comun.js';

const ventasOk = new Counter('ventas_confirmadas');
const rechazos = new Counter('ventas_rechazadas_4xx');
const errores5xx = new Counter('errores_5xx');

export const options = {
  scenarios: {
    simultaneas: { executor: 'shared-iterations', vus: 100, iterations: 100, maxDuration: '1m' },
  },
  thresholds: {
    ventas_confirmadas: ['count==50'],
    ventas_rechazadas_4xx: ['count==50'],
    errores_5xx: ['count==0'],
  },
};

export default function () {
  const r = http.post(`${BASE_URL}/api/ventas`, ventaDeUnaUnidad(), cabeceras());
  if (r.status === 200) ventasOk.add(1);
  else if (r.status >= 400 && r.status < 500) rechazos.add(1);
  else errores5xx.add(1);
}
