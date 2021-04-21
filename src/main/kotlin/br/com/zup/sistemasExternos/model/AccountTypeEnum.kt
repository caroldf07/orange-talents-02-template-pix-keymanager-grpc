package br.com.zup.sistemasExternos.model

import br.com.zup.pix.model.TipoContaEnum

enum class AccountTypeEnum {
    CACC,
    SVGS;

    companion object {
        fun by(tipoContaEnum: TipoContaEnum): AccountTypeEnum {
            return when (tipoContaEnum) {
                TipoContaEnum.CONTA_CORRENTE -> CACC
                else -> SVGS
            }
        }
    }
}
