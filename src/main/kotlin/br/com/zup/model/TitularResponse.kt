package br.com.zup.model

import io.micronaut.core.annotation.Introspected

@Introspected
class TitularResponse(
    val id: String,
    val nome: String,
    val cpf: String,
)
