package br.com.zup.pix.compartilhado.handler

import br.com.zup.pix.compartilhado.exception.ClienteInexistenteException
import br.com.zup.pix.compartilhado.exception.interceptor.ExceptionHandler
import br.com.zup.pix.compartilhado.exception.interceptor.ExceptionHandler.StatusWithDetails
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class ClienteInexistenteExceptionHandler : ExceptionHandler<ClienteInexistenteException> {
    override fun handle(e: ClienteInexistenteException): StatusWithDetails {
        return StatusWithDetails(Status.INVALID_ARGUMENT.withDescription(e.message).withCause(e))
    }

    override fun supports(e: Exception): Boolean {
        return e is ClienteInexistenteException
    }
}