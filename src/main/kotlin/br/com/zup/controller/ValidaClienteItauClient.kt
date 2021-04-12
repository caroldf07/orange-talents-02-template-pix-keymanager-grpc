package br.com.zup.controller

import br.com.zup.model.ClienteItauResponse
import io.micronaut.configuration.hystrix.annotation.HystrixCommand
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.client.annotation.Client
import io.micronaut.retry.annotation.Retryable

@Client("localhost:9091/api/v1/clientes/")
@Retryable
interface ValidaClienteItauClient {

    @Get("{clienteId}/contas")
    @HystrixCommand
    fun consulta(@PathVariable("clienteId") clienteId: String): HttpResponse<ClienteItauResponse>
}