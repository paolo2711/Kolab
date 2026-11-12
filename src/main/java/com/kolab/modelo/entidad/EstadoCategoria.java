package com.kolab.modelo.entidad;

/**
 * Si una categoría se puede seguir usando. Las categorías no se borran: se desactivan, porque las
 * solicitudes antiguas siguen apuntando a ellas.
 */
public enum EstadoCategoria {

    ACTIVA,
    INACTIVA
}
