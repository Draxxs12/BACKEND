# MFA por correo — configuración local

El login y la recuperación de contraseña utilizan SMTP para enviar códigos de 6 dígitos.

## Gmail

La cuenta que envía los mensajes debe tener activada la Verificación en 2 pasos. Google indica que las contraseñas de aplicación son de 16 caracteres y solo están disponibles para cuentas con Verificación en 2 pasos. Una contraseña de aplicación se debe utilizar en lugar de la contraseña normal de Gmail cuando la aplicación necesita autenticarse por SMTP.

1. Entra en la Cuenta de Google que utilizará la aplicación.
2. Activa Verificación en 2 pasos.
3. Crea una contraseña de aplicación para la ferretería.
4. No subas esa contraseña a GitHub.

## Windows PowerShell

Antes de arrancar Spring Boot, configura las variables en la misma terminal:

```powershell
$env:MAIL_HOST="smtp.gmail.com"
$env:MAIL_PORT="587"
$env:MAIL_USERNAME="midanale12@gmail.com"
$env:MAIL_PASSWORD="PEGA_AQUI_LA_APP_PASSWORD"
$env:MAIL_FROM="midanale12@gmail.com"
```

Luego:

```powershell
./mvnw spring-boot:run
```

Para Windows CMD:

```cmd
set MAIL_HOST=smtp.gmail.com
set MAIL_PORT=587
set MAIL_USERNAME=midanale12@gmail.com
set MAIL_PASSWORD=PEGA_AQUI_LA_APP_PASSWORD
set MAIL_FROM=midanale12@gmail.com
mvnw.cmd spring-boot:run
```

La contraseña de aplicación debe conservarse únicamente en las variables de entorno o en el mecanismo de secretos del servidor.

## Flujo

Login:
correo + contraseña -> código MFA por correo -> verificación -> JWT.

Recuperación:
correo -> código por correo -> nueva contraseña.

Los códigos caducan a los 10 minutos, permiten hasta 5 intentos y se invalidan después de utilizarse.
