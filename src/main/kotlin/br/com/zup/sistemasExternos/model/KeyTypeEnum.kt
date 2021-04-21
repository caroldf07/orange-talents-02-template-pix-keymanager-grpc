package br.com.zup.sistemasExternos.model

import br.com.zup.pix.model.TipoChaveEnum

enum class KeyTypeEnum {
    CPF,
    PHONE,
    EMAIL,
    RANDOM;

    companion object {
        fun by(tipoChaveEnum: TipoChaveEnum): KeyTypeEnum {
            return when (tipoChaveEnum) {
                TipoChaveEnum.CPF -> CPF
                TipoChaveEnum.CELULAR -> PHONE
                TipoChaveEnum.EMAIL -> EMAIL
                TipoChaveEnum.ALEATORIA -> RANDOM
            }
        }
    }
}
