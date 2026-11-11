#!/usr/bin/env python3
"""Genera un JWT HS256 igual al que emite JwtUtil.generarToken (sub, rol, usuarioId, iat, exp).

Sirve para las pruebas de rendimiento, donde el login real exige un código MFA enviado por correo.
Solo usa la biblioteca estándar. Variables de entorno:
  JWT_SECRET  clave en Base64, la MISMA que usa el backend (propiedad jwt.secret)
  EMAIL       correo de un usuario ACTIVO con rol Administrador que exista en la base de pruebas
  USUARIO_ID  (opcional) id de ese usuario, por defecto 1
  HORAS       (opcional) validez en horas, por defecto 4 (el backend usa 8)
Uso (PowerShell):  $env:TOKEN = python k6\\generar_token.py
NO usar contra producción ni guardar el resultado en el repositorio.
"""
import base64
import hashlib
import hmac
import json
import os
import sys
import time


def b64url(datos: bytes) -> str:
    return base64.urlsafe_b64encode(datos).rstrip(b"=").decode()


def main() -> None:
    try:
        clave = base64.b64decode(os.environ["JWT_SECRET"])
        email = os.environ["EMAIL"]
    except KeyError as faltante:
        sys.exit(f"Falta la variable de entorno {faltante}")
    if len(clave) < 32:
        sys.exit("JWT_SECRET debe tener al menos 32 bytes (256 bits) una vez decodificada de Base64")

    ahora = int(time.time())
    cabecera = b64url(json.dumps({"alg": "HS256", "typ": "JWT"}, separators=(",", ":")).encode())
    cuerpo = b64url(json.dumps({
        "sub": email,
        "rol": "Administrador",
        "usuarioId": int(os.environ.get("USUARIO_ID", "1")),
        "iat": ahora,
        "exp": ahora + int(float(os.environ.get("HORAS", "4")) * 3600),
    }, separators=(",", ":")).encode())
    firma = b64url(hmac.new(clave, f"{cabecera}.{cuerpo}".encode(), hashlib.sha256).digest())
    print(f"{cabecera}.{cuerpo}.{firma}")


if __name__ == "__main__":
    main()
