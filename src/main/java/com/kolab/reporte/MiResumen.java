package com.kolab.reporte;

import java.math.BigDecimal;
import java.util.List;

/**
 * Cómo le va a una persona, de los dos lados de su cuenta. Solo trae lo que no se ve en otra
 * pantalla: la reputación está en el perfil, las listas en Mi actividad y los mensajes en el menú.
 * Todo sale de sus solicitudes, ofertas y servicios; nada se guarda aparte.
 */
public record MiResumen(Pendientes pendientes, LoQuePido pido, LoQueOfrezco ofrezco) {

    /**
     * Lo que está esperando que la persona haga algo.
     */
    public record Pendientes(long ofertasPorRevisar, long serviciosPorCerrar, long porCalificar) {

        public boolean hayAlgo() {
            return ofertasPorRevisar + serviciosPorCerrar + porCalificar > 0;
        }
    }

    /**
     * Una barra de un gráfico: su rótulo, el valor y qué tan larga es frente a la más larga, de 0 a 100.
     */
    public record Barra(String rotulo, BigDecimal valor, int largo) {
    }

    /**
     * @param diferenciaConLoPropuesto cuánto se cerró, en promedio, por encima (positivo) o por
     *                                 debajo (negativo) del precio que propuso; {@code null} sin tratos
     * @param horasHastaPrimeraOferta  cuánto tardó en llegar la primera oferta, en promedio;
     *                                 {@code null} si nunca le ofertaron
     */
    public record LoQuePido(long publicadas, long conTrato, BigDecimal pagado,
                            BigDecimal diferenciaConLoPropuesto, Double horasHastaPrimeraOferta,
                            List<Barra> pagadoPorCategoria) {

        public boolean vacio() {
            return publicadas == 0;
        }

        public int getTasaDeTrato() {
            return publicadas == 0 ? 0 : (int) Math.round(conTrato * 100.0 / publicadas);
        }

        public boolean negociasteAbajo() {
            return diferenciaConLoPropuesto != null && diferenciaConLoPropuesto.signum() < 0;
        }

        public boolean cerrasteIgual() {
            return diferenciaConLoPropuesto != null && diferenciaConLoPropuesto.signum() == 0;
        }

        public BigDecimal getDiferenciaSinSigno() {
            return diferenciaConLoPropuesto == null ? BigDecimal.ZERO : diferenciaConLoPropuesto.abs();
        }

        public String getEsperaPrimeraOferta() {
            if (horasHastaPrimeraOferta == null) {
                return "—";
            }
            if (horasHastaPrimeraOferta < 1) {
                return "menos de 1 h";
            }
            if (horasHastaPrimeraOferta < 48) {
                return Math.round(horasHastaPrimeraOferta) + " h";
            }
            return Math.round(horasHastaPrimeraOferta / 24) + " días";
        }
    }

    /**
     * @param diferenciaConLoPedido cuánto proponen de más (positivo) o de menos (negativo) frente al
     *                              precio que se pedía, en promedio; {@code null} sin ofertas
     */
    public record LoQueOfrezco(long ofertasEnviadas, long ofertasAceptadas, BigDecimal ganado,
                               long clientes, long clientesQueVolvieron, BigDecimal diferenciaConLoPedido,
                               List<Barra> ganadoPorMes, List<Barra> ganadoPorCategoria) {

        public boolean vacio() {
            return ofertasEnviadas == 0;
        }

        // de cada cien ofertas, cuántas terminaron en trato
        public int getTasaDeAceptacion() {
            return ofertasEnviadas == 0 ? 0 : (int) Math.round(ofertasAceptadas * 100.0 / ofertasEnviadas);
        }

        public boolean ofertasArriba() {
            return diferenciaConLoPedido != null && diferenciaConLoPedido.signum() > 0;
        }

        public boolean ofertasIgual() {
            return diferenciaConLoPedido != null && diferenciaConLoPedido.signum() == 0;
        }

        public BigDecimal getDiferenciaSinSigno() {
            return diferenciaConLoPedido == null ? BigDecimal.ZERO : diferenciaConLoPedido.abs();
        }
    }
}
