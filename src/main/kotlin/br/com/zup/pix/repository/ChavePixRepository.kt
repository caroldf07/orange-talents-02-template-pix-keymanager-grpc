package br.com.zup.pix.repository

import br.com.zup.pix.model.ChavePix
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ChavePixRepository : JpaRepository<ChavePix, UUID> {
    fun existsByValorChave(valorChave: String?): Boolean
    fun findByIdAndIdentificadorItau(id: UUID, identificadorItau: UUID): Optional<ChavePix>
}
