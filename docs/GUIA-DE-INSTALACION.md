# Guía de instalación y despliegue de KOLAB

Para quien instala KOLAB en su PC, lo prueba o lo publica en Azure. Para usar la aplicación está la
[guía de uso](GUIA-DE-USO.md).

## Requisitos

- JDK 21 o superior (https://adoptium.net).
- PostgreSQL 16 o superior, con el instalador de Windows (https://www.postgresql.org/download/windows/).
  Durante la instalación pide una clave para el usuario `postgres`: anótala.
- Git y VS Code con la extensión Extension Pack for Java.

Maven no hace falta, el wrapper `mvnw` va incluido.

## Instalar en la PC

1. Bajar el código:

       git clone https://github.com/paolo2711/Kolab.git

2. Doble clic en `instalar-base.cmd`, en la raíz. Propone el puerto de tu PostgreSQL (Enter para aceptar)
   y pide la clave de `postgres`. Crea la base `kolab` con dos roles: `kolab_migracion`, que crea las
   tablas, y `kolab_app`, que solo lee y escribe datos. Guarda en variables de entorno de tu usuario lo que
   la aplicación necesita para conectarse, y pone `KOLAB_DESARROLLO=true`.
3. Cerrar VS Code y volver a abrirlo, para que vea las variables nuevas.

## Ejecutar

En VS Code, Run sobre `src/main/java/com/kolab/KolabApplication.java`, o por consola:

    mvnw.cmd spring-boot:run

Después, http://localhost:8080. La primera vez Flyway crea las tablas y, como es una PC de desarrollo, la
base se llena con datos de ejemplo. Todas las cuentas de `src/main/resources/ejemplos/personas.json`
entran con `kolab1234`; el administrador es `admin@kolab.pe` y entra por http://localhost:8080/admin/login.

En desarrollo, la pantalla de ingreso muestra las cuentas de ejemplo y dos botones: reiniciar datos (borra
todo y carga los ejemplos de nuevo) y cargar ejemplos (solo si la base está vacía). Nada de eso existe en
el servidor.

## Pruebas

    mvnw.cmd test

Corren contra H2 en memoria con las mismas migraciones, así que no necesitan PostgreSQL. Son 225, en
cuatro tipos que se pueden correr por separado:

    mvnw.cmd test -Dgroups=unitaria
    mvnw.cmd test -Dgroups=controlador
    mvnw.cmd test -Dgroups=integracion
    mvnw.cmd test -Dgroups=seguridad

El reporte en HTML se arma con `mvnw.cmd surefire-report:report` y queda en
`target/reports/surefire.html`. El análisis de seguridad del código propio es
`mvnw.cmd compile spotbugs:check` (SpotBugs con FindSecBugs).

La documentación del código en HTML se genera con `mvnw.cmd javadoc:javadoc` y queda en
`target/reports/apidocs/index.html`.

## Variables de entorno

La configuración va en variables de entorno, nunca en el código.

| Variable | Para qué | Si falta |
|---|---|---|
| `KOLAB_DB_URL` | Dirección JDBC de la base | No arranca |
| `KOLAB_DB_USER`, `KOLAB_DB_PASSWORD` | Usuario y clave con que trabaja la aplicación | No arranca |
| `KOLAB_MIGRACION_USER`, `KOLAB_MIGRACION_PASSWORD` | Usuario con que Flyway crea las tablas | Usa el de la aplicación |
| `KOLAB_DESARROLLO` | `true` solo en una PC de desarrollo | `false` |
| `KOLAB_URL_PUBLICA` | Dirección de KOLAB, con la que se arman los enlaces de los correos | `http://localhost:8080` |
| `KOLAB_BREVO_CLAVE` | Clave de la API de Brevo para mandar correos | No se manda correo |
| `KOLAB_CORREO_REMITENTE` | Correo verificado en Brevo que sale como remitente | No se manda correo |
| `KOLAB_CLAVE_ENLACES` | Firma los enlaces para recuperar la contraseña | Se genera una al arrancar y los enlaces mueren al reiniciar |
| `KOLAB_FOTOS_SUBIDAS` | Carpeta de las fotos que suben las personas | `kolab-fotos` en la carpeta del usuario |
| `KOLAB_COMISION` | Comisión de la plataforma, en porcentaje | `5` |
| `KOLAB_INGRESO_INTENTOS`, `KOLAB_INGRESO_BLOQUEO` | Fallos seguidos que bloquean una cuenta y por cuántos minutos | `5` y `15` |

## Desplegar en Azure

KOLAB corre en Azure App Service (Linux, Java 21) y la base en Azure Database for PostgreSQL con servidor
flexible. Todo se hace desde el portal, sin consola.

1. Crear el servidor PostgreSQL: Desarrollo/pruebas, tamaño B1ms, PostgreSQL 16, con autenticación de
   PostgreSQL. Dentro del servidor, en Bases de datos, crear la base `kolab`. En Redes, permitir el acceso
   desde los servicios de Azure.
2. Crear la App Web: publicar como código, pila Java 21 SE, Linux, plan Gratis F1.
3. En la App Web, Variables de entorno: las de la tabla de arriba y además `SERVER_PORT=80`,
   `SERVER_FORWARD_HEADERS_STRATEGY=native`, `LOGGING_LEVEL_COM_KOLAB=INFO`,
   `KOLAB_FOTOS_SUBIDAS=/home/kolab-fotos` y `SPRING_FLYWAY_LOCATIONS=classpath:db/migration/comun`. La
   última salta la migración de permisos de `kolab_app`, porque en Azure se trabaja con el usuario que crea
   Azure. La URL de la base termina en `?sslmode=require`. `KOLAB_DESARROLLO` no se pone nunca.
4. Configuración, Configuración de la pila, comando de inicio:

       cd /home && java -jar /home/site/wwwroot/app.jar

   `/home` es la única carpeta que App Service conserva al reiniciar; ahí quedan los logs y las fotos.
5. Centro de implementación: GitHub, repositorio `paolo2711/Kolab`, rama `develop`, con autenticación
   básica. Azure agrega el workflow `.github/workflows/develop_kolab-g4.yml`.
6. Para usarla se sube al plan Básico B1 (Escalar verticalmente) y al terminar se vuelve a Gratis F1. Antes
   de bajar a F1 hay que apagar Siempre activado, en Configuración general.

Desde ahí, cada cambio que entra a `develop` se publica solo: el workflow corre `mvn clean package`, que
ejecuta todas las pruebas; si alguna falla, no se publica.

Para el correo de recuperación, en Brevo se genera una clave de la API restringida a Transactional email y
se cargan `KOLAB_BREVO_CLAVE`, `KOLAB_CORREO_REMITENTE` y `KOLAB_URL_PUBLICA` en la App Web.

## Monitoreo

- Logs: Logback escribe cada evento con su nivel en `logs/kolab.log` y en JSON (formato ECS) en
  `logs/kolab.json`. Rotan cada día, se cortan a los 10 MB y se guardan 14 días, con 500 MB como máximo
  (`logback-spring.xml`).
- Historial: pantalla del panel que lee `logs/kolab.json`, con filtro por nivel y buscador.
- Monitoreo: pantalla del panel con las métricas de la aplicación, la base y el servidor, tomadas de
  Micrometer, del pool HikariCP y del sistema operativo.
- Salud: `/actuator/health` responde `UP` cuando la aplicación y la base están bien.
- Errores: `ManejoDeErrores` escribe cada error inesperado como ERROR con su traza y muestra a la persona
  una página sin detalles internos.

## Mantenimiento

- Copias de la base: Azure saca una copia completa cada día y la guarda 7 días. Para restaurar, en el
  servidor, Restaurar: crea un servidor nuevo con los datos del momento elegido; se apunta `KOLAB_DB_URL` a
  ese servidor.
- Revisión de salud: `TareasProgramadas` corre cada hora (`@Scheduled`, hora de Lima) y anota el estado en el
  log; avisa con WARN si la memoria o el disco pasan del 80 % o si la base no responde. La hora se cambia con
  `kolab.revision-cron`.
- Librerías: revisar cada mes con Snyk y OWASP Dependency-Check; cada actualización va en su rama con las
  pruebas en verde.
- Nueva versión: fusionar un Pull Request en `develop`. Volver a la anterior: en GitHub Actions, volver a
  correr el workflow de esa versión.
