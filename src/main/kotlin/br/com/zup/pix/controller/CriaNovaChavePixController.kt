package br.com.zup.pix.controller

import br.com.zup.KeyManagerServiceGrpc
import br.com.zup.NovaChavePixRequest
import br.com.zup.NovaChavePixResponse
import br.com.zup.pix.compartilhado.exception.ChavePixExistenteException
import br.com.zup.pix.compartilhado.exception.ClienteInexistenteException
import br.com.zup.pix.compartilhado.handler.ErrorHandler
import br.com.zup.pix.dominio.NovaChavePixDto
import br.com.zup.pix.model.ChavePix
import br.com.zup.pix.repository.ChavePixRepository
import br.com.zup.pix.service.NovaChavePixService
import br.com.zup.pix.toModel
import io.grpc.stub.StreamObserver
import io.micronaut.http.client.exceptions.HttpClientResponseException
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

//carga total 10
@ErrorHandler //Annotation criada para que a ExceptionInterceptor funcione da maneira planejada
@Singleton
class CriaNovaChavePixController(
    @Inject val service: NovaChavePixService,
    @Inject val repository: ChavePixRepository
) :
    KeyManagerServiceGrpc.KeyManagerServiceImplBase() {

    private val logger = LoggerFactory.getLogger(CriaNovaChavePixController::class.java)

    //6
    override fun criaChavePix(request: NovaChavePixRequest, responseObserver: StreamObserver<NovaChavePixResponse>) {
        logger.info("Nova solicitação recebida")

        val novaChave: NovaChavePixDto = request.toModel() //Transformando o que veio pelo gRPC em dto interno

        //1
        if (repository.existsByValorChave(novaChave.valorChave)) {
            logger.error("Erro na busca")
            throw ChavePixExistenteException("Chave já cadastrada")
        }

        //1
        var chaveCriada: ChavePix?

        //1
        try {
            chaveCriada = service.registra(novaChave)
            //1
        } catch (e: HttpClientResponseException) {
            logger.error("Chave não encontrada")
            throw ClienteInexistenteException("Cliente inexistente")
        }
        responseObserver.onNext(
            NovaChavePixResponse.newBuilder()
                .setPixId(chaveCriada.id.toString())
                .build()
        )

        logger.info("Solicitação finalizada")
        responseObserver.onCompleted()

    }
}


