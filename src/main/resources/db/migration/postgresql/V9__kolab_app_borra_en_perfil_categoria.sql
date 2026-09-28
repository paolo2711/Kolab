-- Unica excepcion al "kolab_app no borra filas": quitar una categoria declarada es quitar una
-- relacion, no destruir un dato. Guardar "que sabes hacer" reemplaza la lista entera, asi que
-- necesita borrar las anteriores.
-- El resto de tablas sigue sin DELETE: una solicitud se cancela cambiando su estado.
grant delete on perfil_categoria to kolab_app;
