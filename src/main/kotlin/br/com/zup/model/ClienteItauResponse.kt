package br.com.zup.model

import io.micronaut.core.annotation.Introspected

@Introspected
class ClienteItauResponse(val tipo:TipoContaEnum, val titularResponse: TitularResponse)
