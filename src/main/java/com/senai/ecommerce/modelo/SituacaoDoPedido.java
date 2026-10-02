package com.senai.ecommerce.modelo;

// ciclo de vida mínimo do pedido: nasce ABERTO, vira PAGO quando o
// pagamento é aprovado (Pedido.pagar) e nunca volta para ABERTO
public enum SituacaoDoPedido {
    ABERTO,
    PAGO
}
