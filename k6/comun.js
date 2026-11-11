// Utilidades compartidas por los escenarios k6.
// Variables de entorno: BASE_URL (por defecto http://localhost:8080) y TOKEN (JWT obtenido con login + verify-mfa).
// IMPORTANTE: SecurityConfig restringe cada ruta por rol (ventas: ADMINISTRADOR/RECEPCIONISTA; productos y compras:
// ADMINISTRADOR/ALMACENERO; reportes: ADMINISTRADOR). Como los escenarios mezclan estas rutas, el TOKEN debe ser de un
// usuario con rol ADMINISTRADOR. Con k6/generar_token.py se genera uno sin pasar por el correo MFA.
export const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export function cabeceras() {
  return {
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${__ENV.TOKEN}`,
    },
  };
}

// Cuerpo de una venta de 1 unidad (formato de VentaRequest del backend).
export function ventaDeUnaUnidad() {
  return JSON.stringify({
    usuarioId: Number(__ENV.USUARIO_ID || 1),
    venta: {
      tipoPago: 'Efectivo',
      tipoComprobante: 'Boleta',
      cliente: { id: Number(__ENV.CLIENTE_ID || 1) },
    },
    detalles: [
      {
        producto: { id: Number(__ENV.PRODUCTO_ID || 1) },
        cantidad: 1,
        precioUnitario: Number(__ENV.PRECIO || 15),
      },
    ],
  });
}
