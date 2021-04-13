package br.com.zup.sistemasExternos.model

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
}

data class TitularResponse(val nome: String, val cpf: String)
data class InstituicaoResponse(val nome: String, val ispb: String)