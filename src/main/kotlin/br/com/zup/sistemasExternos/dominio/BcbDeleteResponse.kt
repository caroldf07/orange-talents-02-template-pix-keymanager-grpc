package br.com.zup.sistemasExternos.dominio

import java.time.LocalDateTime

data class BcbDeleteResponse (val key: String, val deletedAt: LocalDateTime)
