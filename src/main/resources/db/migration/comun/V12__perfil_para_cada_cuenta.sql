-- el perfil se crea con la cuenta; las que ya existian sin perfil reciben uno vacio
insert into perfil (id_usuario, calif_promedio, total_calificaciones)
select u.id_usuario, 0, 0
from usuario u
where not exists (select 1 from perfil p where p.id_usuario = u.id_usuario);
