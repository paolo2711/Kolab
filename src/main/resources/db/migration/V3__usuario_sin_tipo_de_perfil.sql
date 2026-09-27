-- el actor es uno solo: la misma cuenta pide y ofrece. quien ofrece es quien declaro categorias,
-- no quien eligio una casilla al registrarse
alter table usuario drop constraint ck_usuario_tipo_perfil;
alter table usuario drop column tipo_perfil;
