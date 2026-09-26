package com.senai.ecommerce.modelo.pagamento;

import java.math.BigDecimal;

/**
 * Capacidade extra: nem toda forma de pagamento pode ser estornada
 * (dinheiro em espécie, por exemplo, não pode). Por isso é uma segunda
 * interface, e não mais um método em {@link ProcessadorPagamento} —
 * quem não suporta estorno simplesmente não a implementa, em vez de
 * herdar um método que lançaria "operação não suportada".
 */
public interface Estornavel {
    boolean estornar(BigDecimal valor);
}
