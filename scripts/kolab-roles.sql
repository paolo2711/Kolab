-- KOLAB: roles y permisos de la base de datos
-- Se ejecuta una vez por servidor, con psql y como superusuario, después de crear la base kolab:
--   psql -U postgres -d kolab -v clave_migracion=... -v clave_app=... -f kolab-roles.sql
-- Las contraseñas no se escriben en este archivo ni en el repositorio.

-- Dueño del esquema: lo usan solo las migraciones (Flyway) para crear y cambiar tablas.
create role kolab_migracion login password :'clave_migracion';
grant create, usage on schema public to kolab_migracion;

-- Cuenta con la que se conecta la aplicación: lee y escribe datos, pero no puede
-- crear, cambiar ni borrar tablas. Tampoco puede borrar filas: una solicitud se
-- cancela cambiando su estado, no eliminándola.
--
-- Una sola excepción: perfil_categoria. Quitar una categoría declarada es quitar una
-- relación, no destruir un dato, y guardar "qué sabes hacer" reemplaza la lista entera.
-- Ese permiso se otorga en la migración V9, cuando la tabla ya existe.
create role kolab_app login password :'clave_app';
grant connect on database kolab to kolab_app;
grant usage on schema public to kolab_app;

alter default privileges for role kolab_migracion in schema public
    grant select, insert, update on tables to kolab_app;
alter default privileges for role kolab_migracion in schema public
    grant usage, select on sequences to kolab_app;

-- Nadie más puede crear objetos en el esquema público.
revoke create on schema public from public;
