package br.com.zup.pix.compartilhado.handler

import br.com.zup.pix.compartilhado.exception.ChavePixNaoExistenteException
import br.com.zup.pix.compartilhado.exception.interceptor.ExceptionHandler
import br.com.zup.pix.compartilhado.exception.interceptor.ExceptionHandler.StatusWithDetails
import io.grpc.Metadata
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class ChavePixNaoExistenteExceptionHandler : ExceptionHandler<ChavePixNaoExistenteException> {
    override fun handle(e: ChavePixNaoExistenteException): StatusWithDetails {
        return StatusWithDetails(Status.NOT_FOUND.withDescription(e.message).withCause(e), metadata = Metadata())
    }

    override fun supports(e: Exception): Boolean {
        return e is ChavePixNaoExistenteException
    }
}