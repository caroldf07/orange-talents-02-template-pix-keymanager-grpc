package br.com.zup.pix

import br.com.zup.NovaChavePixRequest
import br.com.zup.pix.dominio.NovaChavePixDto
import br.com.zup.pix.model.TipoChaveEnum
import br.com.zup.pix.model.TipoContaEnum

/*Como não conseguimos acessar o Request por ser uma classe criado pelo frame, nós usamos o Extensions para fazer a conversão
* desse request para outra mais manipulável*/
fun NovaChavePixRequest.toModel(): NovaChavePixDto {
    return NovaChavePixDto(
        identificadorItau = identificadorItau,
        tipoChave = when (tipoChave) {
            NovaChavePixRequest.TipoChave.CHAVE_UNKNOWN -> null
            else -> TipoChaveEnum.valueOf(tipoChave.name)
        },
        valorChave = valorChave,
        tipoConta = when (tipoConta) {
            NovaChavePixRequest.TipoConta.CONTA_UNKNOWN -> null
            else -> TipoContaEnum.valueOf(tipoConta.name)
        }
    )
}
