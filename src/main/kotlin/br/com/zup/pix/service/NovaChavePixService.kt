package br.com.zup.pix.service

import br.com.zup.pix.compartilhado.exception.ChavePixExistenteException
import br.com.zup.pix.compartilhado.exception.ClienteInexistenteException
import br.com.zup.pix.dominio.NovaChavePixDto
import br.com.zup.pix.model.ChavePix
import br.com.zup.pix.model.TipoChaveEnum
import br.com.zup.pix.repository.ChavePixRepository
import br.com.zup.sistemasExternos.client.BcbClient
import br.com.zup.sistemasExternos.client.ItauClient
import br.com.zup.sistemasExternos.dominio.BcbResponse
import br.com.zup.sistemasExternos.model.DadosContaItau
import br.com.zup.sistemasExternos.model.DadosContaItauResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.exceptions.HttpClientResponseException
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


        val contaValidada: DadosContaItau =
            contaValidar.body()?.toModel()
                ?: throw ClienteInexistenteException("Cliente inexistente")

        logger.info("Cliente ok")
        val chave: ChavePix = novaChave.toModel(contaValidada)

        logger.info("Validando chave")
        val bcbResponse: HttpResponse<BcbResponse>?
        try {
            bcbResponse =
                bcbClient.cadastraChavePix(contaValidar.body()!!.toBcbRequest(novaChave))
        } catch (e: HttpClientResponseException) {
            throw ChavePixExistenteException("Chave já registrada")
        }

        if (chave.tipoChave == TipoChaveEnum.ALEATORIA) {
            chave.chaveAleatoria(bcbResponse.body()!!.key) // verificar se isso seria uma brecha ou não se segurança
        }

        logger.info("Chave validada")
        chavePixRepository.save(chave)

        logger.info("Chave criada")
        return chave
    }

}
