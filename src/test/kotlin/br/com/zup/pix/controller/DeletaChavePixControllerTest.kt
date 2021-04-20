package br.com.zup.pix.controller

import br.com.zup.ChavePixRequest.newBuilder
import br.com.zup.ChavePixResponse
import br.com.zup.KeyManagerDeleteServiceGrpc
import br.com.zup.KeyManagerDeleteServiceGrpc.KeyManagerDeleteServiceBlockingStub
import br.com.zup.pix.model.ChavePix
import br.com.zup.pix.model.TipoChaveEnum
import br.com.zup.pix.model.TipoContaEnum
import br.com.zup.pix.repository.ChavePixRepository
import br.com.zup.sistemasExternos.model.DadosContaItau
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

@MicronautTest(transactional = false)
internal class DeletaChavePixControllerTest(
    val repository: ChavePixRepository,
    val grpcClient: KeyManagerDeleteServiceBlockingStub
) {

    /*
    * 1 - Happy Path - ok
    * 2 - Não deve deletar se algum campo estiver nulo ou vazio - ok
    * 3 - Não deve deletar se não existir o pixId - ok
    * 4 - Não deve deletar se não existir o identificarItau - ok
    * 5 - Não deve deletar se ambas as chaves não existirem
    * */

    lateinit var CHAVE_TESTE: ChavePix

    @BeforeEach
    fun setup() {

        repository.deleteAll() //limpamos o banco, caso o teste anterior o tenha sujado

        CHAVE_TESTE = repository.save(
            ChavePix(
                identificadorItau = UUID.randomUUID(),
                tipoChave = TipoChaveEnum.CPF,
                valorChave = "02467781054",
                tipoConta = TipoContaEnum.CONTA_CORRENTE,
                DadosContaItau(
                    instituicao = "ITAÚ UNIBANCO S.A.",
                    nomeTitular = "Rafael M C Ponte",
                    cpfTitular = "02467781054",
                    agencia = "0001",
                    numeroConta = "291900"
                )
            )
        )

    }


    @Test
    @DisplayName("deve deletar a chave pix do banco")
    fun `deve deletar a chave pix do banco`() {
        //cenário

        //ação
        val response: ChavePixResponse = grpcClient.deletaChavePix(
            newBuilder().setPixId(CHAVE_TESTE.id.toString())
                .setIdentificadorItau(CHAVE_TESTE.identificadorItau.toString()).build()
        )
        //validação
        with(response) {
            assertFalse(repository.existsById(CHAVE_TESTE.id!!))
        }
    }

    @Test
    @DisplayName("não deve deletar se algum campo estiver nulo ou vazio")
    fun `não deve deletar se algum campo estiver nulo ou vazio`() {
        //cenário

        //ação
        val response = assertThrows<StatusRuntimeException> {
            grpcClient.deletaChavePix(
                newBuilder().setPixId("")
                    .setIdentificadorItau("").build()
            )
        }
        //validação
        assertEquals(Status.INVALID_ARGUMENT.code, response.status.code)
    }

    @Test
    @DisplayName("não deve deletar se não existir o pixId")
    fun `não deve deletar se não existir o pixId`() {
        //cenário

        //ação
        val response = assertThrows<StatusRuntimeException> {
            grpcClient.deletaChavePix(
                newBuilder()
                    .setPixId(UUID.randomUUID().toString())
                    .setIdentificadorItau(CHAVE_TESTE.identificadorItau.toString())
                    .build()
            )
        }
        //validação
        assertEquals(Status.NOT_FOUND.code, response.status.code)
        assertTrue(response.message!!.contains("A chave informada não existe ou não pertence ao cliente"))
        assertTrue(repository.existsById(CHAVE_TESTE.id!!))
    }

    @Test
    @DisplayName("não deve deletar se não existir o identificadorItau")
    fun `não deve deletar se não existir o identificadorItaupixId`() {
        //cenário

        //ação
        val response = assertThrows<StatusRuntimeException> {
            grpcClient.deletaChavePix(
                newBuilder()
                    .setPixId(CHAVE_TESTE.id.toString())
                    .setIdentificadorItau(UUID.randomUUID().toString())
                    .build()
            )
        }
        //validação
        assertEquals(Status.NOT_FOUND.code, response.status.code)
        assertTrue(response.message!!.contains("A chave informada não existe ou não pertence ao cliente"))
        assertTrue(repository.existsById(CHAVE_TESTE.id!!))
    }

    @Test
    @DisplayName("não deve deletar se ambas não existirem")
    fun `não deve deletar se ambas não existirem`() {
        //cenário

        //ação
        val response = assertThrows<StatusRuntimeException> {
            grpcClient.deletaChavePix(
                newBuilder()
                    .setPixId(UUID.randomUUID().toString())
                    .setIdentificadorItau(UUID.randomUUID().toString())
                    .build()
            )
        }
        //validação
        assertEquals(Status.NOT_FOUND.code, response.status.code)
        assertTrue(response.message!!.contains("A chave informada não existe ou não pertence ao cliente"))
        assertTrue(repository.existsById(CHAVE_TESTE.id!!))
    }

    @Factory
    class Clients {

        @Bean
        fun blockingStrub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerDeleteServiceGrpc.KeyManagerDeleteServiceBlockingStub {
            return KeyManagerDeleteServiceGrpc.newBlockingStub(channel)
        }
    }

}