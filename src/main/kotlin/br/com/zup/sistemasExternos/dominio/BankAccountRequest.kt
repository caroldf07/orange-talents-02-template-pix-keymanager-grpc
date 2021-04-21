package br.com.zup.sistemasExternos.dominio

import br.com.zup.sistemasExternos.model.AccountTypeEnum
import io.micronaut.core.annotation.Introspected

@Introspected
class BankAccountRequest(
    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: AccountTypeEnum
)
