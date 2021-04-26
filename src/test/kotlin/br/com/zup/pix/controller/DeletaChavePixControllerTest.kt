package br.com.zup.pix.controller

import br.com.zup.ChavePixRequest
import br.com.zup.KeyManagerDeleteServiceGrpc
import br.com.zup.pix.model.ChavePix
import br.com.zup.pix.model.TipoChaveEnum
import br.com.zup.pix.model.TipoContaEnum
import br.com.zup.pix.repository.ChavePixRepository
import br.com.zup.sistemasExternos.client.BcbClient
import br.com.zup.sistemasExternos.dominio.BcbDeleteRequest
import br.com.zup.sistemasExternos.model.DadosContaItau
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.util.*
import javax.inject.Inject


@MicronautTest(transactional = false)
internal class DeletaChavePixControllerTest(
    @Inject val grpcClient: KeyManagerDeleteServiceGrpc.KeyManagerDeleteServiceBlockingStub,
    @Inject val repository: ChavePixRepository
) {
    /*
    * Testes a serem feitos:
    * 1 - Happy Path - ok
    * 2 - Não pode deletar chave quando campo inválido - ok
    * 3 - Deve dar erro quando chave não existir no banco de dados - ok
    * 4 - Deve dar erro quando identificador itaú não existir no banco de dados - ok
    * 5 - Deve dar erro quando chave não existir no bcb - ok
    * */

    val pixId = UUID.randomUUID()
    val identificadorItau = UUID.randomUUID()
    val tipoConta = TipoContaEnum.CONTA_CORRENTE
    val tipoChave = TipoChaveEnum.CPF
    val valorChave = "02467781054"
    val instituicao = "ITAÚ UNIBANCO S.A."
    val ispb = "60701190"
    val agencia = "0001"
    val numeroConta = "291900"
    val nomeTitular = "Rafael M C Ponte"
    val cpf = "02467781054"

    val bcbDeleteRequest = BcbDeleteRequest(key = valorChave, participant = ispb)

    val chave = ChavePix(
        identificadorItau = identificadorItau,
        tipoChave = tipoChave,
        valorChave = valorChave,
        tipoConta = tipoConta,
        DadosContaItau(
            instituicao = instituicao,
            nomeTitular = nomeTitular,
            cpfTitular = cpf,
            agencia = agencia,
            numeroConta = numeroConta
        )
    )


    @BeforeEach
    fun setup() {
        repository.save(chave)
    }

    @AfterEach
    fun clean() {
        repository.deleteAll()
    }

    @Inject
    lateinit var bcbClient: BcbClient

    @MockBean(BcbClient::class)
    fun deletaChavePix(): BcbClient {
        return mock(BcbClient::class.java)
    }

    @Test
    @DisplayName("deve deletar uma chave pix")
    fun `deve deletar uma chave pix`() {
        //cenário
        `when`(bcbClient.deletaChavePix(key = cpf, bcbDeleteRequest = bcbDeleteRequest)).thenReturn(HttpResponse.ok())

        //ação
        grpcClient.deletaChavePix(
            ChavePixRequest.newBuilder().setPixId(chave.id.toString())
                .setIdentificadorItau(chave.identificadorItau.toString())
                .build()
        )
        //validação
        assertFalse(repository.existsByValorChave(valorChave))
    }

    @Test
    @DisplayName("não pode deletar chave quando campo inválido")
    fun `não pode deletar chave quando campo inválido`() {
        //cenário

        //ação
        val response = assertThrows<StatusRuntimeException> {
            grpcClient.deletaChavePix(
                ChavePixRequest.newBuilder().setPixId("")
                    .setIdentificadorItau("")
                    .build()
            )
        }
        //válidação
        assertEquals(Status.INVALID_ARGUMENT.code, response.status.code)
        assertTrue(repository.existsByValorChave(valorChave))
    }

    @Test
    @DisplayName("deve dar erro quando chave não existir no banco de dados")
    fun `deve dar erro quando chave não existir no banco de dados`() {
        //cenário

        //ação

        val response = assertThrows<StatusRuntimeException> {
            grpcClient.deletaChavePix(
                ChavePixRequest.newBuilder().setPixId(UUID.randomUUID().toString())
                    .setIdentificadorItau(identificadorItau.toString()).build()
            )
        }

        //validação
        assertEquals(Status.NOT_FOUND.code, response.status.code)
        assertTrue(response.message!!.contains("A chave informada não existe ou não pertence ao cliente"))
    }

    @Test
    @DisplayName("deve dar erro quando identificador itau não existir no banco de dados")
    fun `deve dar erro quando identificador itau não existir no banco de dados`() {
        //cenário

        //ação

        val response = assertThrows<StatusRuntimeException> {
            grpcClient.deletaChavePix(
                ChavePixRequest.newBuilder().setPixId(chave.id.toString())
                    .setIdentificadorItau(UUID.randomUUID().toString()).build()
            )
        }

        //validação
        assertEquals(Status.NOT_FOUND.code, response.status.code)
        assertTrue(response.message!!.contains("A chave informada não existe ou não pertence ao cliente"))
    }

    @Test
    @DisplayName("deve dar erro quando chave não existir no bcb")
    fun `deve dar erro quando chave não existir no bcb`() {
        //cenário
        `when`(
            bcbClient.deletaChavePix(
                key = valorChave,
                bcbDeleteRequest = bcbDeleteRequest
            )
        ).thenReturn(HttpResponse.notFound())
        //ação

        val response = assertThrows<StatusRuntimeException> {
            grpcClient.deletaChavePix(
                ChavePixRequest.newBuilder().setPixId(chave.id.toString())
                    .setIdentificadorItau(chave.identificadorItau.toString()).build()
            )
        }

        //validação
        assertEquals(Status.FAILED_PRECONDITION.code, response.status.code)
        assertTrue(response.message!!.contains("Algo deu errado"))
        assertTrue(repository.existsByValorChave(chave.valorChave))
    }

    @Factory
    class Client {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerDeleteServiceGrpc.KeyManagerDeleteServiceBlockingStub {
            return KeyManagerDeleteServiceGrpc.newBlockingStub(channel)
        }
    }

}