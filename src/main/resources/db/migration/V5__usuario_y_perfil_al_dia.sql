-- usuario: donde esta la persona, para ordenar por cercania las solicitudes presenciales
alter table usuario add column distrito varchar(60);
alter table usuario add column latitud numeric(9, 6);
alter table usuario add column longitud numeric(9, 6);

alter table usuario alter column fecha_registro set default current_timestamp;
alter table usuario alter column estado set default 'ACTIVO';

alter table usuario add constraint ck_usuario_latitud check (latitud between -90 and 90);
alter table usuario add constraint ck_usuario_longitud check (longitud between -180 and 180);

-- perfil: el promedio y el total se actualizan al registrar una calificacion, no se recalculan
-- cada vez que alguien abre un perfil
alter table perfil add column total_calificaciones integer not null default 0;

alter table perfil add constraint ck_perfil_calif check (calif_promedio between 0 and 5);
alter table perfil add constraint ck_perfil_total check (total_calificaciones >= 0);

-- perfil_categoria va a tener dos claves foraneas, asi que la que ya existe lleva su apellido
alter table perfil_categoria rename constraint fk_perfil_categoria to fk_perfil_categoria_perfil;
