# Registro de cambios

Todo lo que cambia en KOLAB se anota aquí. El formato sigue
[Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/) y las versiones, el
[versionado semántico](https://semver.org/lang/es/).

## [Sin publicar]

## [0.3.2]

### Cambiado

- La clave de las cuentas que arman las pruebas está en una sola constante y no repetida en cada
  clase.

### Seguridad

- Al escribir desde el chat de una solicitud o de un servicio, el formulario solo dice a qué
  pantalla volver y su id. La ruta la arma la aplicación, y cualquier otro valor se queda en la
  conversación.

## [0.3.1]

### Seguridad

- Dependencias subidas a la última versión que funciona con Spring Boot 3.5.16: POI, Commons Lang,
  Log4j, Jackson, el driver de PostgreSQL, Hibernate Validator, Guava y Logback. Spring Framework y
  Spring Security quedan como están.
- Al escribir un mensaje, el destino de vuelta solo se sigue si es una ruta de la aplicación. Uno de
  afuera manda a la bandeja de Mensajes.

## [0.3.0]

### Agregado

- Rol de administrador con su propio ingreso y su panel: cuentas, solicitudes abiertas,
  servicios, comisión del mes y categorías más pedidas, con descarga en Excel.
- Suspender y reactivar cuentas. Una suspendida no entra y si estaba adentro se le corta la sesión.
- Foto opcional en la solicitud (JPG o PNG, hasta 2 MB), revisada por contenido y guardada fuera
  del jar. Sin foto sale la de la categoría.
- Foto de perfil y portada en Editar perfil. Sin portada, la franja de arriba toma el color de la
  foto.
- Vista previa en Publicar y Editar con lo ya escrito y la foto elegida.
- Abrir en Mensajes desde el chat chico, y Visto debajo del último mensaje propio.
- Las propuestas directas sin responder salen arriba con «Para ti», y lo publicado hace menos de
  48 horas lleva «Nuevo».
- Enlaces al perfil de la otra persona desde servicios, mensajes y reseñas.

### Cambiado

- Las pantallas usan todo el ancho. Mensajes tiene la lista a la izquierda y el chat a la derecha.
- El perfil se crea con la cuenta (migración V12 para las que ya había). El directorio solo muestra
  a quien tiene alguna categoría.
- Lo que ofrezco dice qué pasó con cada oferta.
- Los chats chicos usan las mismas burbujas y el mismo botón que Mensajes.
- El fondo de los perfiles es una franja de color según el nombre, no la foto de una categoría.

### Corregido

- La vista previa de Publicar y los chats de las ofertas no funcionaban: sus scripts estaban en la
  plantilla y la política de contenido los bloqueaba. Lo mismo el aviso antes de reiniciar los
  datos en modo desarrollo.
- En el panel, un formulario sin token daba 404 en vez de 403.

### Seguridad

- Los perfiles van por un código público al azar y no por el id de la base, que dejaba recorrer
  las cuentas una por una. Por id responde 404.
- Sin `'unsafe-inline'` en la política de contenido. Hay pruebas que fallan si vuelve un `style=`,
  un script escrito en la plantilla o un atributo `on*`.
- Pruebas de seguridad de punta a punta: rutas privadas, cosas ajenas, CSRF y bloqueo por intentos.
- Tomcat 10.1.61.

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
