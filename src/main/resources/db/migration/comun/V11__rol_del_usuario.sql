-- quien administra la plataforma es una cuenta mas con otro rol; todas las demas son USUARIO
alter table usuario add column rol varchar(20) default 'USUARIO' not null;

alter table usuario add constraint ck_usuario_rol check (rol in ('USUARIO', 'ADMINISTRADOR'));
