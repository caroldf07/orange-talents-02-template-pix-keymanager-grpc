package br.com.zup.pix.service

import br.com.zup.pix.compartilhado.exception.ChavePixExistenteException
import br.com.zup.pix.compartilhado.exception.ClienteInexistenteException
import br.com.zup.pix.dominio.NovaChavePixDto
import br.com.zup.pix.model.ChavePix
import br.com.zup.pix.repository.ChavePixRepository
import br.com.zup.sistemasExternos.client.BcbClient
import br.com.zup.sistemasExternos.client.ItauClient
import br.com.zup.sistemasExternos.dominio.BcbResponse
import br.com.zup.sistemasExternos.model.DadosContaItau
import br.com.zup.sistemasExternos.model.DadosContaItauResponse
import io.micronaut.http.HttpResponse
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Singleton
@Validated
class NovaChavePixService(
    @Inject val itauClient: ItauClient,
    @Inject val chavePixRepository: ChavePixRepository,
    @Inject val bcbClient: BcbClient

) {
    val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun registra(@Valid novaChave: NovaChavePixDto): ChavePix {

        logger.info("Validando cliente")
        val contaValidar: HttpResponse<DadosContaItauResponse> =
            itauClient.validaCliente(novaChave.identificadorItau, novaChave.tipoConta!!.name)

        logger.info("Retorno obtido")
        val contaValidada: DadosContaItau =
            contaValidar.body()?.toModel()
                ?: throw ClienteInexistenteException("Cliente inexistente")

        logger.info("Validando chave")
        val bcbResponse: HttpResponse<BcbResponse> =
            bcbClient.cadastraChavePix(contaValidar.body().toBcbRequest(novaChave))
        val chaveCadastradaBcb: BcbResponse =
            bcbResponse.body() ?: throw ChavePixExistenteException("Chave já cadastrada")

        logger.info("Retorno obtido")

        val chave: ChavePix = novaChave.toModel(contaValidada)
        chave.chaveAleatoria(chaveCadastradaBcb.key)

        chavePixRepository.save(chave)

        logger.info("Chave criada")
        return chave
    }

}
