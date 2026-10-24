# Registro de cambios

Todo lo que cambia en KOLAB se anota aquí. El formato sigue
[Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/) y las versiones, el
[versionado semántico](https://semver.org/lang/es/).

## [Sin publicar]

## [0.2.0]

### Agregado

- Flujo principal completo contra PostgreSQL: crear cuenta, ingresar, recuperar la contraseña,
  declarar qué sabe hacer cada uno, editar el perfil, publicar una solicitud con su precio,
  explorar con filtros, ofertar, aceptar o rechazar, conversar dentro de la oferta, cerrar el
  servicio y calificarse. Ninguna pantalla lee datos de archivos.
- Mi resumen: cómo le va a cada persona de los dos lados de su cuenta, calculado al consultar.
- Descarga de Mi resumen en Excel, con una hoja por cada lado y los montos como número.
- Registro de los eventos de las cuentas en `logs/kolab.log`, que rota por día y por tamaño y
  guarda catorce días. Van al logger `kolab.seguridad`: ingreso correcto y fallido, cuenta
  bloqueada, pedido de enlace de recuperación, cambio de contraseña y pedidos de algo ajeno. El
  correo se escribe enmascarado y nunca se registra una contraseña ni un enlace.
- Bloqueo del ingreso tras cinco intentos fallidos seguidos con el mismo correo, por quince
  minutos. Se levanta solo. El mensaje al usuario es el mismo de siempre, así que no revela si la
  cuenta existe.
- Cabeceras de seguridad: política de contenido, `X-Content-Type-Options`, `Referrer-Policy`,
  bloqueo de marcos y HSTS. Las pantallas de error dejaron de mostrar la traza y el mensaje de la
  excepción.
- Perfil `https` para levantar la aplicación en el 8443 con un certificado local, que crea
  `scripts/crear-certificado-local.ps1` y no entra al repositorio.
- OWASP Dependency-Check y versions-maven-plugin, los dos para correr a mano, sin colgarse de la
  compilación de todos los días.
- Base de datos con dos roles separados: uno migra y otro es el que usa la aplicación, que no
  puede borrar tablas.

### Cambiado

- Las herramientas de desarrollo (reiniciar la base y cargar los ejemplos) solo existen con el
  modo desarrollo encendido y solo responden desde la propia máquina.
- El armado del nombre de archivo de una foto y de las iniciales de una persona pasó a Commons
  Lang, en vez de normalizar el texto a mano.
- Las pruebas dejaron de depender de cómo está configurada la máquina: antes fallaban en toda PC
  que hubiera corrido el instalador.
- Se quitaron los métodos y las reglas de estilo que ya no usaba nadie.
