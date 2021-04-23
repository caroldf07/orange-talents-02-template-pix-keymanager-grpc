package br.com.zup.sistemasExternos.dominio

import br.com.zup.sistemasExternos.model.KeyTypeEnum

data class BcbRequest(
    val keyType: KeyTypeEnum,
    val key: String,
    val bankAccount: BankAccountRequest,
    val owner: OwnerRequest
)
