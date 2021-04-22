package br.com.zup.pix.model

import br.com.zup.sistemasExternos.dominio.BankAccountRequest
import br.com.zup.sistemasExternos.dominio.BcbRequest
import br.com.zup.sistemasExternos.dominio.OwnerRequest
import br.com.zup.sistemasExternos.model.AccountTypeEnum
import br.com.zup.sistemasExternos.model.DadosContaItau
import br.com.zup.sistemasExternos.model.KeyTypeEnum
import br.com.zup.sistemasExternos.model.TypeEnum
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
    var valorChave: String,

    @field:NotNull
    @field:Enumerated(EnumType.STRING)
    val tipoConta: TipoContaEnum,

    @field:NotNull
    @field:Embedded
    val conta: DadosContaItau
) {
    fun chaveAleatoria(key: String): ChavePix {
        this.valorChave = key
        return ChavePix(this.identificadorItau, this.tipoChave, valorChave, this.tipoConta, this.conta)
    }

    @Id
    @GeneratedValue
    val id: UUID? = null

    fun toBcbRequest(): BcbRequest {
        return BcbRequest(
            keyType = KeyTypeEnum.by(this.tipoChave),
            key = this.valorChave,
            BankAccountRequest(
                participant = "60701190",
                branch = this.conta.agencia,
                accountNumber = this.conta.numeroConta,
                accountType = AccountTypeEnum.by(this.tipoConta)
            ),
            OwnerRequest(
                type = TypeEnum.NATURAL_PERSON,
                name = this.conta.nomeTitular,
                taxIdNumber = this.conta.cpfTitular
            )
        )
    }
}
