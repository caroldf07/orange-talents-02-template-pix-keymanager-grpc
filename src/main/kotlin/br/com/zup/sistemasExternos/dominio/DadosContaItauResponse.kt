package br.com.zup.sistemasExternos.model

import br.com.zup.pix.dominio.NovaChavePixDto
import br.com.zup.sistemasExternos.dominio.BankAccountRequest
import br.com.zup.sistemasExternos.dominio.BcbRequest
import br.com.zup.sistemasExternos.dominio.OwnerRequest

data class DadosContaItauResponse(
    val tipo: String,
    val instituicao: InstituicaoResponse,
    val agencia: String,
    val numero: String,
    val titular: TitularResponse
) {
    fun toModel(): DadosContaItau {
        return DadosContaItau(
            instituicao = this.instituicao.nome,
            nomeTitular = this.titular.nome,
            cpfTitular = this.titular.cpf,
            agencia = this.agencia,
            numeroConta = this.numero
        )
    }

    fun toBcbRequest(novaChavePixDto: NovaChavePixDto): BcbRequest {
        return BcbRequest(
            keyType = KeyTypeEnum.valueOf(novaChavePixDto.tipoChave!!.name),
            key = novaChavePixDto.valorChave.toString(),
            BankAccountRequest(
                participant = this.instituicao.ispb,
                branch = this.agencia,
                accountNumber = this.numero,
                accountType = AccountTypeEnum.valueOf(this.tipo)
            ),
            OwnerRequest(
                type = TypeEnum.NATURAL_PERSON,
                name = this.titular.nome,
                taxIdNumber = this.titular.cpf
            )
        )
    }
}

data class TitularResponse(val nome: String, val cpf: String)
data class InstituicaoResponse(val nome: String, val ispb: String)