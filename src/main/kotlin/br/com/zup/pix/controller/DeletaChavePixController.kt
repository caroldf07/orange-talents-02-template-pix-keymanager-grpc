package br.com.zup.pix.controller

import br.com.zup.ChavePixRequest
import br.com.zup.ChavePixResponse
import br.com.zup.KeyManagerDeleteServiceGrpc
import br.com.zup.pix.compartilhado.handler.ErrorHandler
import br.com.zup.pix.service.ServiceDelete
import io.grpc.Status
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class DeletaChavePixController(@Inject val serviceDelete: ServiceDelete) :
    KeyManagerDeleteServiceGrpc.KeyManagerDeleteServiceImplBase() {
    private val logger = LoggerFactory.getLogger(DeletaChavePixController::class.java)

    override fun deletaChavePix(request: ChavePixRequest, responseObserver: StreamObserver<ChavePixResponse>) {
        logger.info("Requisição recebida")

        serviceDelete.procuraChaveDelete(request.pixId, request.identificadorItau)

        responseObserver.onNext(
            ChavePixResponse
                .newBuilder()
                .setStatus(Status.Code.OK.toString())
                .build()
        )
        logger.info("Exclusão concluída com sucesso")
        responseObserver.onCompleted()
    }
}