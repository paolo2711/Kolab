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
    ├── demo            lectura de los datos de ejemplo en JSON (temporal)
    └── common          utilidades y excepciones propias

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

El esquema está versionado con Flyway en `src/main/resources/db/migration`. Cada archivo se aplica
una vez y no se reescribe nunca: para cambiar algo se agrega el siguiente número.

`spring.jpa.hibernate.ddl-auto=validate` está puesto a propósito. Hibernate no crea ni modifica
tablas, solo comprueba al arrancar que las entidades y el esquema coincidan. Si alguien cambia una
entidad y se olvida de la migración, la aplicación falla al arrancar en vez de corromper datos
callada.

Dos roles: `kolab_migracion` es dueño del esquema y lo usa Flyway para crear y cambiar tablas;
`kolab_app` es con el que se conecta la aplicación y solo puede leer, insertar y actualizar. No
puede borrar filas: una solicitud se cancela cambiando su estado.

Las pruebas corren contra H2 en memoria con las mismas migraciones, así que no hace falta un
PostgreSQL levantado para compilar.

## Los datos de ejemplo

Las pantallas todavía leen de `src/main/resources/datos/*.json` a través del paquete `demo`, que
tiene un cargador (`ArchivosDeDatos`) y seis lectores, uno por dominio. Conectarlas a la base es el
siguiente paso.

Cuando toque, cada lector cambia su fuente por el repositorio correspondiente y los controladores
no se tocan. Por eso están partidos así y no en una sola clase.

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

`scripts\crear-base-local.ps1` crea la base, ejecuta `kolab-roles.sql` y deja las cinco variables
con `setx`. **Solo pide la clave de `postgres`**: las de los dos roles las genera al azar, 32
caracteres, y quedan únicamente en las variables de entorno. Nadie las escribe ni las necesita a
mano. Se puede volver a correr: si los roles ya existen les cambia la clave y actualiza las
variables.

    powershell -ExecutionPolicy Bypass -File scripts\crear-base-local.ps1

Espera psql en `D:\Program Files\PostgreSQL\18\bin` y el puerto 5433. Si están en otro sitio:

    powershell -ExecutionPolicy Bypass -File scripts\crear-base-local.ps1 -Psql "C:\ruta\psql.exe" -Puerto 5432

Las cinco variables que deja:

| Variable | Para qué |
|---|---|
| `KOLAB_DB_URL` | la conexión, con el puerto |
| `KOLAB_DB_USER`, `KOLAB_DB_PASSWORD` | `kolab_app`, con el que corre la aplicación |
| `KOLAB_MIGRACION_USER`, `KOLAB_MIGRACION_PASSWORD` | `kolab_migracion`, con el que migra Flyway |

Con `setx` hay que abrir una terminal nueva para que tomen efecto. Después:

    mvnw.cmd -B clean package
    java -jar target/kolab.jar

Las pruebas no necesitan ninguna de estas variables.
