package br.com.zup.sistemasExternos.model

import br.com.zup.pix.model.TipoChaveEnum

enum class KeyTypeEnum(val tipoChaveEnum: TipoChaveEnum) {
    CPF(TipoChaveEnum.CPF),
    PHONE(TipoChaveEnum.CELULAR),
    EMAIL(TipoChaveEnum.EMAIL),
    RANDOM(TipoChaveEnum.ALEATORIA)
}
