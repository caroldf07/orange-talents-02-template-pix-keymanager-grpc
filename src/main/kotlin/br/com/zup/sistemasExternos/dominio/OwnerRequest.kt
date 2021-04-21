package br.com.zup.sistemasExternos.dominio

import br.com.zup.sistemasExternos.model.TypeEnum
import io.micronaut.core.annotation.Introspected

@Introspected
class OwnerRequest(val type: TypeEnum, val name: String, val taxIdNumber: String)