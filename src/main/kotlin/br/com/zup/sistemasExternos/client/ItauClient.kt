package br.com.zup.sistemasExternos.client

import br.com.zup.sistemasExternos.model.DadosContaItauResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client
import io.micronaut.retry.annotation.Retryable

@Client("\${sistemaExterno.itau.url}")
interface ItauClient {

    @Get(
        value = "api/v1/clientes/{clienteId}/contas",
        produces = [MediaType.APPLICATION_JSON],
        consumes = [MediaType.APPLICATION_JSON]
    )
    @Retryable
    fun validaCliente(
        @PathVariable("clienteId") identificadorItau: String, @QueryValue("tipo")
        tipo: String
    ): HttpResponse<DadosContaItauResponse>

}