package br.com.zup.pix.controller

import br.com.zup.KeyManagerServiceGrpc
import br.com.zup.NovaChavePixRequest
import br.com.zup.NovaChavePixResponse
import br.com.zup.pix.dominio.NovaChavePixDto
import br.com.zup.pix.model.ChavePix
import br.com.zup.pix.service.NovaChavePixService
import br.com.zup.pix.toModel
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CriaNovaChavePixController(@Inject val service: NovaChavePixService) :
    KeyManagerServiceGrpc.KeyManagerServiceImplBase() {

    private val logger = LoggerFactory.getLogger(CriaNovaChavePixController::class.java)

    override fun criaChavePix(request: NovaChavePixRequest, responseObserver: StreamObserver<NovaChavePixResponse>) {
        logger.info("Nova solicitação recebida")

        val novaChave: NovaChavePixDto = request.toModel() //Transformando o que veio pelo gRPC em dto interno
        val chaveCriada: ChavePix = service.registra(novaChave)

        responseObserver.onNext(
            NovaChavePixResponse.newBuilder()
                .setPixId(chaveCriada.pixId.toString())
                .build()
        )

        logger.info("Solicitação finalizada")
        responseObserver.onCompleted()

    }
}


