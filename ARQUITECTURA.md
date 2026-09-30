# Arquitectura de KOLAB

Monolito de un solo repositorio: backend, vistas y estilos viven juntos y se despliegan como un
`.jar`. Spring Boot 3.5 sobre Java 21, plantillas Thymeleaf, PostgreSQL.

## Módulos

Los paquetes van por dominio, no por capa. Cada uno lleva su entidad, su repositorio, su servicio
y su controlador, así que para entender una funcionalidad se abre una sola carpeta.

    com.kolab
    ├── config          seguridad y configuración
    ├── usuario         cuenta, registro, ingreso y sesión
    ├── perfil          perfil, reputación y lo que la persona sabe hacer
    ├── categoria       catálogo de rubros y precios de referencia
    ├── solicitud       lo que alguien necesita, con su precio
    ├── oferta          la respuesta de quien sabe hacerlo
    ├── servicio        el trato cerrado, su avance y su cierre
    ├── mensaje         la conversación de cada oferta
    ├── calificacion    la nota de cada parte al terminar
    ├── inicio          portada e inicio del usuario
    ├── reporte         indicadores de la plataforma y el resumen de cada persona
    ├── desarrollo      datos de ejemplo y herramientas; solo con KOLAB_DESARROLLO=true
    └── common          utilidades, fotos y excepciones propias

Un paquete puede tener dos controladores cuando la misma entidad se usa desde dos lados. Pasa en
`solicitud`: `SolicitudController` es el catálogo donde se explora y se oferta, y
`MisSolicitudesController` es lo que hace el autor con las suyas.

## Capas

    Navegador
       │
    Controlador      recibe la petición, arma el modelo, elige la vista. No toca el repositorio.
       │
    Servicio         las reglas del negocio. Interfaz e implementación separadas.
       │
    Repositorio      Spring Data JPA. Es la capa DAO, no hay otra encima.
       │
    PostgreSQL

Las entidades no salen a las vistas cuando hay un formulario de por medio: para eso están las
clases `*Form` con Bean Validation. Para mostrar datos van registros `*Resumen` y `*Detalle`, que
llevan solo lo que la pantalla necesita.

## Base de datos

Nueve tablas: `usuario`, `perfil`, `categoria`, `perfil_categoria`, `solicitud`, `oferta`,
`servicio`, `mensaje` y `calificacion`. Más la vista `v_precios_categoria`, que calcula al
consultar lo que se está pagando en cada rubro.

El esquema está versionado con Flyway. Cada archivo se aplica una vez y no se reescribe nunca:
para cambiar algo se agrega el siguiente número. Van en dos carpetas:

- `db/migration/comun` corre en cualquier motor. Ahí está el esquema.
- `db/migration/postgresql` corre solo contra PostgreSQL. Ahí van los permisos, que no tienen
  equivalente en H2 y romperían las pruebas.

`spring.jpa.hibernate.ddl-auto=validate` está puesto a propósito. Hibernate no crea ni modifica
tablas, solo comprueba al arrancar que las entidades y el esquema coincidan. Si alguien cambia una
entidad y se olvida de la migración, la aplicación falla al arrancar en vez de corromper datos
callada.

Dos roles: `kolab_migracion` es dueño del esquema y lo usa Flyway para crear y cambiar tablas;
`kolab_app` es con el que se conecta la aplicación y solo puede leer, insertar y actualizar. No
puede borrar filas: una solicitud se cancela cambiando su estado. La única excepción es
`perfil_categoria`, donde sí borra, porque quitar una categoría declarada es quitar una relación.

Las pruebas corren contra H2 en memoria con las mismas migraciones, así que no hace falta un
PostgreSQL levantado para compilar.

## Los datos de ejemplo

Todas las pantallas leen y escriben en la base. Los JSON de `src/main/resources/ejemplos` no los
lee ninguna pantalla: son la semilla con la que se llena una base vacía en una máquina de desarrollo.

La carga está en el paquete `desarrollo` y pasa por los mismos servicios que usa la aplicación
(registrar, publicar, ofertar, aceptar, cerrar y calificar), así los ejemplos cumplen las mismas
reglas que los datos reales. Los archivos se apuntan entre sí por correo, por nombre de categoría o
por una clave propia, nunca por id.

Solo carga si la tabla `solicitud` está vacía: arrancar dos veces no duplica nada. Para que entre un
cambio en los JSON se usa «reiniciar datos», que borra el esquema con el rol de migraciones, vuelve
a migrar y carga de nuevo.

Todo el paquete depende de `kolab.desarrollo`. Fuera de desarrollo sus rutas no existen.

## Fotos

Las fotos se encuentran por nombre, sin columna en la base. La de una categoría se llama como la
categoría (`img/categoria/hogar-y-reparaciones.jpg`), la de una persona como su correo antes de la
arroba (`img/persona/luis.mendoza.jpg`) y la de las pantallas de acceso es `img/acceso.jpg`. Si
falta el archivo se muestra el ícono de la categoría o las iniciales.

## Agregar una pantalla

1. El registro de vista en el paquete del dominio: un `record` con lo que la pantalla muestra, no
   la entidad.
2. El método que lo arma, en el servicio del dominio.
3. El método del controlador: `@GetMapping`, llena el modelo, devuelve el nombre de la plantilla.
4. La plantilla en `templates/<dominio>/`, con `layout :: base` y los fragmentos de
   `fragmentos/componentes.html`. El nombre del archivo dice qué pantalla es.
5. Si la ruta es pública, agregarla a `SecurityConfig`. Si no, queda protegida sola.
6. Estilos en `static/css/kolab.css`, clases con prefijo `k-`. Buscar el nombre antes de
   inventarlo: reusar uno que ya existe rompe la pantalla en silencio.

## Correr con PostgreSQL en Windows

La conexión sale de variables de entorno. No hay contraseñas en el repositorio ni en los scripts.

`instalar-base.cmd` llama a `scripts\crear-base-local.ps1`, que busca psql en `C:` o `D:`, pregunta el
puerto, crea la base, ejecuta `kolab-roles.sql` y deja las variables con `setx`. Solo pide la clave
de `postgres`: las de los dos roles las genera al azar y quedan únicamente en las variables. Se
puede volver a correr: si los roles ya existen les cambia la clave.

Las variables que deja:

- `KOLAB_DB_URL`: la conexión, con el puerto.
- `KOLAB_DB_USER` y `KOLAB_DB_PASSWORD`: `kolab_app`, con el que corre la aplicación.
- `KOLAB_MIGRACION_USER` y `KOLAB_MIGRACION_PASSWORD`: `kolab_migracion`, con el que migra Flyway.
- `KOLAB_DESARROLLO=true`: carga los ejemplos y muestra las herramientas de desarrollo.

Otras que se pueden definir: `KOLAB_COMISION` (porcentaje, 5 si no se define) y
`KOLAB_CLAVE_ENLACES`, que firma los enlaces para recuperar la contraseña. Sin ella se genera una al
arrancar y los enlaces vencen al reiniciar.

Con `setx` hay que cerrar y volver a abrir el editor o la terminal. Las pruebas no necesitan
ninguna de estas variables.
