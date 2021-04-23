package br.com.zup.sistemasExternos.dominio

import br.com.zup.sistemasExternos.model.TypeEnum

data class OwnerRequest(val type: TypeEnum, val name: String, val taxIdNumber: String)