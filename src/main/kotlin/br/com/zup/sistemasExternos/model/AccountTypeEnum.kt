package br.com.zup.sistemasExternos.model

import br.com.zup.pix.model.TipoContaEnum

enum class AccountTypeEnum(val tipoContaEnum: TipoContaEnum) {
    CACC(TipoContaEnum.CONTA_CORRENTE),
    SVGS(TipoContaEnum.CONTA_POUPANCA)
}
