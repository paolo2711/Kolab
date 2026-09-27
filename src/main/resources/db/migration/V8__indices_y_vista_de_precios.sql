-- Indices para las busquedas frecuentes. Las restricciones unique ya crean el suyo, asi que no se
-- repiten aqui.
create index ix_solicitud_estado_categoria on solicitud (estado, id_categoria);
create index ix_solicitud_autor on solicitud (id_autor);
create index ix_solicitud_destinatario on solicitud (id_destinatario);
create index ix_solicitud_fecha on solicitud (fecha_publicacion);
create index ix_oferta_usuario on oferta (id_usuario);
create index ix_perfil_categoria_categoria on perfil_categoria (id_categoria);
create index ix_mensaje_oferta_fecha on mensaje (id_oferta, fecha_envio);
create index ix_mensaje_emisor on mensaje (id_emisor);
create index ix_calificacion_evaluado on calificacion (id_evaluado);

-- Reporte "Cuanto se esta pagando": se calcula al consultar, no se guarda.
-- Si la categoria no tiene solicitudes, se usa el rango de referencia.
-- Cuando el volumen lo pida, esta vista pasa a materializada y se refresca por horario.
create view v_precios_categoria as
select c.id_categoria,
       c.nombre,
       count(s.id_solicitud) as solicitudes,
       coalesce(min(s.precio_propuesto), c.precio_ref_min) as precio_bajo,
       coalesce(percentile_disc(0.5) within group (order by s.precio_propuesto),
                (c.precio_ref_min + c.precio_ref_max) / 2) as precio_tipico,
       coalesce(max(s.precio_propuesto), c.precio_ref_max) as precio_alto
from categoria c
left join solicitud s on s.id_categoria = c.id_categoria and s.estado <> 'CANCELADA'
group by c.id_categoria, c.nombre, c.precio_ref_min, c.precio_ref_max;
