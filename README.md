# KOLAB

Plataforma para intercambiar habilidades y servicios en la que quien pide pone el precio: publicas
lo que necesitas con el monto que quieres pagar, y quien sabe hacerlo lo acepta o te propone otro.
Una misma cuenta sirve para pedir y para ofrecer.

## Requisitos

JDK 21 o superior (https://adoptium.net). Maven no hace falta, el wrapper va incluido.

## Ejecutar

    git clone https://github.com/paolo2711/Kolab.git
    cd Kolab
    mvnw.cmd -B clean package
    java -jar target/kolab.jar

Abrir http://localhost:8080

Desde IntelliJ o Eclipse: abrir como proyecto Maven y ejecutar KolabApplication.

## Usuarios de prueba

    cliente@kolab.pe / kolab1234
    experto@kolab.pe / kolab1234

## Stack

Spring Boot 3.5, Thymeleaf, Spring Security, Spring Data JPA, H2, Flyway, Maven. CSS propio.

El registro y el ingreso trabajan contra la base de datos. Las demás pantallas leen los datos de
ejemplo de src/main/resources/datos.

## Pruebas

    mvnw.cmd -B test
