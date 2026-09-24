# MFA por correo — Gmail API

El login y la recuperación de contraseña envían códigos de 6 dígitos mediante la Gmail API de Google.

## Destinatarios automáticos

La aplicación no configura manualmente el correo de cada usuario.

Cuando un usuario inicia sesión, el backend busca ese usuario en la tabla usuarios y utiliza el valor email de ese registro como destinatario.

Ejemplo:

    dany -> correo-del-usuario
    juan -> correo-del-usuario
    ana  -> correo-del-usuario

Cada persona recibe el código en su propio correo registrado.

## Cuenta emisora

La aplicación utiliza una sola cuenta de Google como cuenta emisora. Esa cuenta se autoriza una sola vez con OAuth 2.0 y la aplicación reutiliza su refresh token para obtener nuevos access tokens.

Google documenta este flujo para aplicaciones de servidor y el método users.messages.send para enviar mensajes.
Referencia: https://developers.google.com/workspace/gmail/api/auth/web-server
Referencia: https://developers.google.com/workspace/gmail/api/guides/sending

## Configuración de Google Cloud

1. Crea o selecciona un proyecto en Google Cloud.
2. Habilita Gmail API.
3. Configura la pantalla de consentimiento OAuth.
4. Crea un OAuth 2.0 Client ID para la aplicación.
5. Autoriza la cuenta que enviará los correos con el alcance mínimo:
   https://www.googleapis.com/auth/gmail.send
6. Obtén un refresh token para esa cuenta emisora.

Google recomienda almacenar el refresh token del lado del servidor y reutilizarlo para solicitudes posteriores sin volver a pedir consentimiento.

## Variables de entorno

PowerShell:

    $env:GOOGLE_GMAIL_CLIENT_ID="TU_CLIENT_ID"
    $env:GOOGLE_GMAIL_CLIENT_SECRET="TU_CLIENT_SECRET"
    $env:GOOGLE_GMAIL_REFRESH_TOKEN="TU_REFRESH_TOKEN"
    $env:GOOGLE_GMAIL_SENDER_EMAIL="correo-emisor@gmail.com"

    ./mvnw spring-boot:run

CMD:

    set GOOGLE_GMAIL_CLIENT_ID=TU_CLIENT_ID
    set GOOGLE_GMAIL_CLIENT_SECRET=TU_CLIENT_SECRET
    set GOOGLE_GMAIL_REFRESH_TOKEN=TU_REFRESH_TOKEN
    set GOOGLE_GMAIL_SENDER_EMAIL=correo-emisor@gmail.com
    mvnw.cmd spring-boot:run

No subas client secret ni refresh token al repositorio.

## Flujo de login

    correo + contraseña
           ↓
    backend busca usuario
           ↓
    usuario.email de la BD
           ↓
    Gmail API envía código
           ↓
    usuario introduce código
           ↓
    backend valida código
           ↓
    JWT + acceso

## Flujo de recuperación

    correo
      ↓
    backend busca usuario
      ↓
    usuario.email de la BD
      ↓
    Gmail API envía código
      ↓
    usuario introduce código
      ↓
    nueva contraseña

Los códigos caducan a los 10 minutos, permiten hasta 5 intentos y se invalidan después de utilizarse.
