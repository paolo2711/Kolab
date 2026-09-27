-- no hay un perfil de experto aparte del perfil de la persona: es uno solo por cuenta
alter table perfil_experto rename to perfil;

alter table perfil rename constraint uk_perfil_experto_usuario to uk_perfil_usuario;
alter table perfil rename constraint fk_perfil_experto_usuario to fk_perfil_usuario;
alter table perfil_categoria rename constraint fk_perfil_categoria_perfil to fk_perfil_categoria;
