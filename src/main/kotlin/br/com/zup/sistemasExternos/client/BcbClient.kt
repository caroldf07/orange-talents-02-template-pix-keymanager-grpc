package br.com.zup.sistemasExternos.client

import br.com.zup.sistemasExternos.dominio.BcbRequest
import br.com.zup.sistemasExternos.dominio.BcbResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Consumes
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Produces
import io.micronaut.http.client.annotation.Client
import io.micronaut.retry.annotation.Retryable

@Client("\${sistemaExterno.bcb.url}")
interface BcbClient {

    @Post
    @Consumes(*[MediaType.APPLICATION_XML])
    @Produces(*[MediaType.APPLICATION_XML])
    @Retryable
    fun cadastraChavePix(@Body bcbRequest: BcbRequest): HttpResponse<BcbResponse>
}
