package br.com.zup.pix.service

import br.com.zup.pix.dominio.NovaChavePixDto
import br.com.zup.pix.model.ChavePix
import br.com.zup.pix.repository.ChavePixRepository
import br.com.zup.sistemasExternos.ItauClient
import br.com.zup.sistemasExternos.model.DadosContaItau
import br.com.zup.sistemasExternos.model.DadosContaItauResponse
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.http.HttpResponse
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
@Validated
class NovaChavePixService(
    @Inject val itauClient: ItauClient,
    @Inject val chavePixRepository: ChavePixRepository
) {

    val logger = LoggerFactory.getLogger(this::class.java)

    fun registra(@Valid novaChave: NovaChavePixDto): ChavePix {
        logger.info("Validando cliente")

        val contaValidar: HttpResponse<DadosContaItauResponse> =
            itauClient.validaCliente(novaChave.identificadorItau, novaChave.tipoConta!!.name)

        logger.info("Retorno obtido")

        val contaValidada: DadosContaItau =
            contaValidar.body()?.toModel()
                ?: throw StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription("Cliente inexistente"))

        val chave: ChavePix = novaChave.toModel(contaValidada)

        chavePixRepository.save(chave)

        logger.info("Chave criada")

        return chave

    }

}
