package br.com.zup.sistemasExternos.client

import br.com.zup.sistemasExternos.dominio.BcbDeleteRequest
import br.com.zup.sistemasExternos.dominio.BcbDeleteResponse
import br.com.zup.sistemasExternos.dominio.BcbRequest
import br.com.zup.sistemasExternos.dominio.BcbResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client
import io.micronaut.retry.annotation.Retryable

@Client("\${sistemaExterno.bcb.url}")
interface BcbClient {

    @Retryable
    @Post(produces = [MediaType.APPLICATION_XML], consumes = [MediaType.APPLICATION_XML])
    fun cadastraChavePix(@Body bcbRequest: BcbRequest): HttpResponse<BcbResponse>

    @Retryable
    @Delete("/{key}", produces = [MediaType.APPLICATION_XML], consumes = [MediaType.APPLICATION_XML])
    fun deletaChavePix(@PathVariable("key") key: String, @Body bcbDeleteRequest: BcbDeleteRequest): HttpResponse<BcbDeleteResponse>
}
