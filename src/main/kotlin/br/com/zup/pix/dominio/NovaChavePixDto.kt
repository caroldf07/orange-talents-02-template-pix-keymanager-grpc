package br.com.zup.pix.dominio

import br.com.zup.pix.compartilhado.KeyPixValida
import br.com.zup.pix.model.ChavePix
import br.com.zup.pix.model.TipoChaveEnum
import br.com.zup.pix.model.TipoContaEnum
import br.com.zup.sistemasExternos.model.DadosContaItau
import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Introspected
@KeyPixValida
data class NovaChavePixDto(
    @field:NotBlank
    val identificadorItau: String,
    @field:NotNull
    val tipoChave: TipoChaveEnum?,
    @field:Size(max = 77)
    val valorChave: String?,
    @field:NotNull
    val tipoConta: TipoContaEnum?
) {
    fun toModel(contaValidada: DadosContaItau): ChavePix {
        return ChavePix(
            identificadorItau = UUID.fromString(this.identificadorItau),
            tipoChave = TipoChaveEnum.valueOf(this.tipoChave!!.name),
            valorChave = if (this.tipoChave == TipoChaveEnum.ALEATORIA) "" else this.valorChave!!,
            tipoConta = TipoContaEnum.valueOf(this.tipoConta!!.name),
            conta = contaValidada
        )
    }
}
