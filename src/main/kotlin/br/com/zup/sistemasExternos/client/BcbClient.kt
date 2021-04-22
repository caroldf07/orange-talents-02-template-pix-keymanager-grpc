package br.com.zup.sistemasExternos.client

import br.com.zup.sistemasExternos.dominio.BcbDeleteRequest
import br.com.zup.sistemasExternos.dominio.BcbRequest
import br.com.zup.sistemasExternos.dominio.BcbResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client
import io.micronaut.retry.annotation.Retryable

@Client("\${sistemaExterno.bcb.url}")
interface BcbClient {

    @Post(produces = [MediaType.APPLICATION_XML], consumes = [MediaType.APPLICATION_XML])
    @Retryable
    fun cadastraChavePix(@Body bcbRequest: BcbRequest): HttpResponse<BcbResponse>

    @Delete("/{key}")
    @Consumes(*[MediaType.APPLICATION_XML])
    @Produces(*[MediaType.APPLICATION_XML])
    @Retryable
    fun deletaChavePix(@PathVariable("key") key: String, @Body bcbDeleteRequest: BcbDeleteRequest): HttpResponse<Any>
}
