# KOLAB

Plataforma para intercambiar habilidades y servicios en la que quien pide pone el precio: publicas
lo que necesitas con el monto que quieres pagar, y quien sabe hacerlo lo acepta o te propone otro.
Una misma cuenta sirve para pedir y para ofrecer.

## Requisitos

- JDK 21 o superior (https://adoptium.net).
- PostgreSQL 16 o superior, con el instalador de Windows (https://www.postgresql.org/download/windows/).
  Durante la instalación te pide una clave para el usuario `postgres`: anótala.

Maven no hace falta, el wrapper va incluido.

## Instalar, una sola vez

1. Doble clic en `instalar-base.cmd`, en la raíz del proyecto. Te propone el puerto de tu PostgreSQL
   (Enter para aceptarlo) y te pide la clave de `postgres`. Crea la base `kolab` y deja guardado lo
   que la aplicación necesita para conectarse. Si la base ya existía, te pide confirmar y la crea
   de cero.
2. Cierra VS Code y vuelve a abrirlo. Sin esto no ve lo que dejó el paso anterior.

## Ejecutar

En VS Code, con la extensión Extension Pack for Java, abre la carpeta del proyecto y dale Run a
`src/main/java/com/kolab/KolabApplication.java`. Después abre http://localhost:8080.

La primera vez la base se llena sola con datos de ejemplo.

## Cuentas de ejemplo

    cliente@kolab.pe / kolab1234
    experto@kolab.pe / kolab1234

Todas las personas de `src/main/resources/ejemplos/personas.json` entran con `kolab1234`.

## Mientras se desarrolla

Al pie de la pantalla de ingreso están las cuentas de ejemplo y dos enlaces:

- reiniciar datos: borra todo y vuelve a cargar los ejemplos. Sirve cuando cambias un JSON de
  `ejemplos` o cuando la base quedó desordenada.
- cargar ejemplos: los carga solo si la base está vacía.

Para recuperar una contraseña todavía no hay correo: después de poner tu correo, la misma pantalla
muestra el enlace para cambiarla.

Nada de esto existe en el servidor. Depende de `KOLAB_DESARROLLO`, que solo deja puesto
`instalar-base.cmd`.

## Pruebas

    mvnw.cmd -B test

Corren contra H2 en memoria con las mismas migraciones, así que no necesitan PostgreSQL.

## Stack

Java 21, Spring Boot 3.5, Thymeleaf, Spring Security, Spring Data JPA, PostgreSQL, Flyway y Maven.
CSS propio.
