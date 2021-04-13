package br.com.zup.sistemasExternos

import br.com.zup.sistemasExternos.model.DadosContaItauResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.client.annotation.Client

@Client("\${sistemaExterno.itau.url}")
interface ItauClient {

    @Get("api/v1/clientes/{clienteId}")
    fun validaCliente(@PathVariable("clienteId") identificadorItau: String): HttpResponse<DadosContaItauResponse>

}
