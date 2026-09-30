# KOLAB

Plataforma para intercambiar habilidades y servicios en la que quien pide pone el precio: publicas
lo que necesitas con el monto que quieres pagar, y quien sabe hacerlo lo acepta o te propone otro.
Una misma cuenta sirve para pedir y para ofrecer.

## Requisitos

JDK 21 o superior (https://adoptium.net) y PostgreSQL 16 o superior. Maven no hace falta, el
wrapper va incluido.

## Ejecutar

Primero hay que crear la base kolab, sus roles y las variables de entorno. Lo hace un script, que
pide las claves al correr:

    powershell -ExecutionPolicy Bypass -File scripts\crear-base-local.ps1

Después, en una terminal nueva:

    git clone https://github.com/paolo2711/Kolab.git
    cd Kolab
    mvnw.cmd -B clean package
    java -jar target/kolab.jar

Abrir http://localhost:8080

Desde IntelliJ o Eclipse: abrir como proyecto Maven y ejecutar KolabApplication.

## Solo probarlo, sin instalar PostgreSQL

    java -jar target\kolab.jar --spring.profiles.active=demo

El perfil demo levanta con la base en memoria: no hace falta PostgreSQL ni variables de entorno.
Se pierde todo al cerrar.

## Usuarios de prueba

    cliente@kolab.pe / kolab1234
    experto@kolab.pe / kolab1234

## Stack

Java 21, Spring Boot 3.5, Thymeleaf, Spring Security, Spring Data JPA, PostgreSQL, Flyway y Maven.
CSS propio. H2 solo en las pruebas, así que para compilar no hace falta PostgreSQL.

El registro y el ingreso trabajan contra la base de datos. Las demás pantallas leen los datos de
ejemplo de src/main/resources/datos.

## Pruebas

    mvnw.cmd -B test
