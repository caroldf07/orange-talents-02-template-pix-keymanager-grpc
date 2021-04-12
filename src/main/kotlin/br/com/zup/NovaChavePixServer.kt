package br.com.zup

import br.com.zup.controller.ValidaClienteItauClient
import br.com.zup.model.ClienteItauResponse
import br.com.zup.repository.ChavePixRepository
import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.micronaut.http.HttpResponse
import org.slf4j.LoggerFactory
import javax.inject.Singleton

@Singleton
class NovaChavePixServer(val chavePixRepository: ChavePixRepository) : ValidaClienteItauClient,
    DesafioPixServiceGrpc.DesafioPixServiceImplBase() {
    //1
    private val logger = LoggerFactory.getLogger(NovaChavePixServer::class.java)

    override fun criaChavePix(request: NovaChavePixRequest?, responseObserver: StreamObserver<NovaChavePixResponse>?) {
        logger.info("Validando novo pedido de cadastro de chave")

        val identificadorItau: String = request!!.identificadorItau
        val tipoChave: NovaChavePixRequest.TipoChave? = request!!.tipoChave
        val valorChave: String = request!!.valorChave
        val tipoConta: NovaChavePixRequest.TipoConta? = request!!.tipoConta
        var erro = null

        //1
        if (identificadorItau == null || identificadorItau.isBlank() || tipoChave == null || valorChave.isBlank() || tipoConta == null) {

            logger.warn("Algum campo veio nulo ou vazio")

            erro = Status.INVALID_ARGUMENT.withDescription("Todos os campos devem ser preenchidos")
                .asRuntimeException() as Nothing?
        }

        logger.info("Consultando cliente")
        println(request.toString())
        val clienteValido: HttpResponse<ClienteItauResponse> = consulta(identificadorItau)

        //1
        if (tipoChave!!.equals("CPF")) {
            //1
            if (!valorChave.matches("^[0-9]{11}$".toRegex()) || valorChave == null) {
                logger.warn("Formato de CPF inválido")

                erro = Status.INVALID_ARGUMENT.withDescription("Formato de CPF inválido")
                    .asRuntimeException() as Nothing?
            }
            //1
        } else if (tipoChave!!.equals("celular")) {
            //1
            if (!valorChave.matches("^\\+[1-9][0-9]\\d{1,14}\$".toRegex()) || valorChave == null) {
                logger.warn("Formato de número de celular inválido")

                erro = Status.INVALID_ARGUMENT.withDescription("Formato de número de celular inválido")
                    .asRuntimeException() as Nothing?
            }
        } else if (tipoChave!!.equals("email")) {
            if (!valorChave.matches(("(?:[a-z0-9!#\$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#\$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@" +
                        "(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:" +
                        "(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])").toRegex()) || valorChave == null
            )
                logger.warn("Formato de e-mail inválido")

            erro = Status.INVALID_ARGUMENT.withDescription("E-mail inválido")
                .asRuntimeException() as Nothing?
        } else if (tipoChave!!.equals("chaveAleatoria")) {
            if (valorChave != null) {
                erro = Status.INVALID_ARGUMENT.withDescription("O valor para chave do tipo aleatório, deve ser vazio")
                    .asRuntimeException() as Nothing?
            }
        }


    }

    /*Consulta no sistema legado se o cliente existe ou não*/
    override fun consulta(clienteId: String): HttpResponse<ClienteItauResponse> {
        return HttpResponse.accepted()
    }
}