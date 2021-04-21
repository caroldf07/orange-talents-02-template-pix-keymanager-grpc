package br.com.zup.sistemasExternos.dominio

import br.com.zup.sistemasExternos.model.KeyTypeEnum
import io.micronaut.core.annotation.Introspected

@Introspected
class BcbRequest(
    val keyType: KeyTypeEnum,
    val key: String,
    val bankAccount: BankAccountRequest,
    val owner: OwnerRequest
)
