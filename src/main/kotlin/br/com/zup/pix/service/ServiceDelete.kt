package br.com.zup.pix.service

import br.com.zup.pix.compartilhado.exception.ChavePixNaoExistenteException
import br.com.zup.pix.repository.ChavePixRepository
import br.com.zup.sistemasExternos.client.BcbClient
import br.com.zup.sistemasExternos.dominio.BcbDeleteRequest
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.constraints.NotBlank

@Validated
@Singleton
class ServiceDelete(@Inject val repository: ChavePixRepository, @Inject val bcbClient: BcbClient) {
    private val logger = LoggerFactory.getLogger(ServiceDelete::class.java)

    @Transactional
    fun procuraChaveDelete(@NotBlank pixId: String, @NotBlank identificadorItau: String) {
        logger.info("Procurando chave informada")

        val chaveInformada =
            repository.findByIdAndIdentificadorItau(UUID.fromString(pixId), UUID.fromString(identificadorItau))
                .orElseThrow {
                    ChavePixNaoExistenteException("A chave informada não existe ou não pertence ao cliente")
                }

        logger.info("Procura concluída")

        bcbClient.deletaChavePix(
            chaveInformada.valorChave,
            BcbDeleteRequest(key = chaveInformada.valorChave, participant = "60701190")
        ).body() ?: throw IllegalStateException("Algo deu errado")

        repository.delete(chaveInformada)

    }

}
