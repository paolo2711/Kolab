-- la foto va en la cuenta porque sale en todas las listas; portada y color solo en el perfil
alter table usuario add column foto varchar(40);
alter table perfil add column portada varchar(40);
-- color principal de la foto en #rrggbb, para la franja cuando no hay portada
alter table perfil add column color_foto varchar(7);
