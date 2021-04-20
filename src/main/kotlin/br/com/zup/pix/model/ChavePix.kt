package br.com.zup.pix.model

import br.com.zup.sistemasExternos.model.DadosContaItau
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
class ChavePix(

    @field:NotNull
    val identificadorItau: UUID,

    @field:NotNull
    @field:Enumerated(EnumType.STRING)
    val tipoChave: TipoChaveEnum,

    @field:NotBlank
    @field:Column(unique = true)
    val valorChave: String,

    @field:NotNull
    @field:Enumerated(EnumType.STRING)
    val tipoConta: TipoContaEnum,

    @field:NotNull
    @field:Embedded
    val conta: DadosContaItau
) {
    @Id
    @GeneratedValue
    val id: UUID? = null
}
