package br.com.zup.sistemasExternos.dominio

import br.com.zup.sistemasExternos.model.AccountTypeEnum

data class BankAccountRequest(
    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: AccountTypeEnum
)
