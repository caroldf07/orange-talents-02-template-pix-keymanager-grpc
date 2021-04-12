package br.com.zup.model

import io.micronaut.core.annotation.Introspected
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Introspected
data class ChavePix(
    @Id @GeneratedValue(strategy = GenerationType.AUTO) val pixId: Long,
    @NotBlank val identificadorItau: String,
    @NotNull @Enumerated(EnumType.STRING) val tipoConta: TipoContaEnum,
    @NotNull @Enumerated(EnumType.STRING) val tipoChave: TipoChaveEnum,
    val valorChave: String,
)
