package br.com.zup.pix.controller

import br.com.zup.KeyManagerServiceGrpc
import br.com.zup.KeyManagerServiceGrpc.KeyManagerServiceBlockingStub
import br.com.zup.NovaChavePixRequest.*
import br.com.zup.NovaChavePixResponse
import br.com.zup.pix.model.ChavePix
import br.com.zup.pix.model.TipoChaveEnum.CPF
import br.com.zup.pix.model.TipoContaEnum.CONTA_CORRENTE
import br.com.zup.pix.repository.ChavePixRepository
import br.com.zup.sistemasExternos.ItauClient
import br.com.zup.sistemasExternos.model.DadosContaItauResponse
import br.com.zup.sistemasExternos.model.InstituicaoResponse
import br.com.zup.sistemasExternos.model.TitularResponse
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.util.*
import javax.inject.Inject

/*
* 1 - Happy path - ok
* 2 - Chave já cadastrada - ok
* 3 - Qualquer campo inválido - ok
* 4 - Client do Itaú não encontra a chave
* */

@MicronautTest(transactional = false)// Quando estamos trabalhando com servidor gRPC, ele roda em uma thread separada e, portanto, não participa a cada chamada do teste o que pode trazer problemas, por isso desligamos o transactional
internal class CriaNovaChavePixControllerTest(
    val repository: ChavePixRepository,
    val grpcClient: KeyManagerServiceBlockingStub
) {

    companion object {
        val IDENTIFICADORITAU = UUID.randomUUID()
    }

    @Inject
    lateinit var itauClient: ItauClient

    private val dadosContaItauResponse =
        DadosContaItauResponse(
            tipo = "CONTA_CORRENTE",
            InstituicaoResponse(nome = "ITAÚ UNIBANCO S.A.", ispb = "60701190"),
            agencia = "0001",
            numero = "212233",
            TitularResponse(nome = "Alberto Tavares", cpf = "06628726061")
        )

    @MockBean(ItauClient::class) //mockamos a chamada para o cliente externo, pois as vezes pode demorar demais a chamada ou então fazer algum tipo de registro que, no momento do teste, não é para acontecer
    fun validaCliente(): ItauClient? {
        return mock(ItauClient::class.java)
    }

    @BeforeEach
    fun setup() {
        repository.deleteAll() //limpamos o banco, caso o teste anterior o tenha sujado
    }

    @Test
    @DisplayName("deve cadastrar uma nova chave")
    fun `deve cadastrar uma nova chave`() {
        //cenário

        `when`(
            itauClient.validaCliente(
                identificadorItau = IDENTIFICADORITAU.toString(),
                tipo = "CONTA_CORRENTE"
            )
        ).thenReturn(
            HttpResponse.ok(dadosContaItauResponse)
        )

        //ação
        val response: NovaChavePixResponse = grpcClient.criaChavePix(
            //Estamos simulando o envio de uma requisição, igual fazemos via Bloom
            newBuilder()
                .setIdentificadorItau(IDENTIFICADORITAU.toString())
                .setTipoChave(TipoChave.CPF)
                .setValorChave("06628726061")
                .setTipoConta(TipoConta.CONTA_CORRENTE)
                .build()
        )
        //validação
        with(response) {//uma forma de deixar o código mais elegante
            assertNotNull(this.pixId)
            assertTrue(repository.existsById(UUID.fromString(this.pixId))) // efeito colateral, estamos validando se a integração realmente ocorreu entre o sistema e o banco de dados

        }
    }

    @Test
    @DisplayName("não deve adicionar nova chave quando chave já existente")
    fun `não deve adicionar nova chave quando chave já existente`() {
        //cenário

        val existente = repository.save(
            ChavePix(
                IDENTIFICADORITAU, CPF, "06628726061", CONTA_CORRENTE,
                dadosContaItauResponse.toModel()
            )
        )
        //ação
        val erro = assertThrows<StatusRuntimeException> {
            grpcClient.criaChavePix(
                newBuilder().setIdentificadorItau(IDENTIFICADORITAU.toString())
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .setTipoChave(TipoChave.CPF)
                    .setValorChave(existente.valorChave)
                    .build()
            )
        } //Como estamos esperando que ele dê um exceção, usamos o assertThrows

        //validação
        with(erro) {
            assertEquals(Status.ALREADY_EXISTS.code, this.status.code)
            assertTrue(this.message!!.contains("Chave já cadastrada"))
        }

    }

    @Test
    @DisplayName("não deve adicionar nova chave quando campo está inválido")
    fun `não deve adicionar nova chave quando campo está inválido`() {
        //cenário

        //ação
        val erro = assertThrows<StatusRuntimeException> {
            grpcClient.criaChavePix(
                newBuilder().setIdentificadorItau("")
                    .setTipoConta(TipoConta.CONTA_UNKNOWN)
                    .setTipoChave(TipoChave.CHAVE_UNKNOWN)
                    .setValorChave("")
                    .build()
            )
        }
        //validação
        assertEquals(Status.INVALID_ARGUMENT.code, erro.status.code)
        assertTrue(!repository.existsByValorChave(""))
    }

    @Test
    @DisplayName("não deve adicionar nova chave quando não encontra o cliente no Itaú")
    fun `não deve adicionar nova chave quando não encontra o cliente no Itaú`() {
        //cenário

        `when`(itauClient.validaCliente(identificadorItau = "1", tipo = "CONTA_CORRENTE")).thenThrow(
            HttpClientResponseException::class.java
        )

        //ação
        val erro = assertThrows<StatusRuntimeException> {
            grpcClient.criaChavePix(
                newBuilder()
                    .setIdentificadorItau("1")
                    .setTipoChave(TipoChave.CPF)
                    .setValorChave("06628726061")
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .build()
            )
        }
        //validação
        assertEquals(Status.INVALID_ARGUMENT.code, erro.status.code)
        assertTrue(erro.message!!.contains("Cliente inexistente"))
    }

    /*Aqui nós fabricamos o nosso client para que possamos testar a integração para a requisição no gRPC.
    A factory is a Singleton that produces one or many other bean implementations.
    Each produced bean is defined by method that is annotated with Bean*/
    @Factory
    class Clients {
        /*Usamos o GrpcServerChannel.NAME, pois, a cada teste, o Micronaut muda a porta do servidor que ele usa, então
        Como não temos como saber qual será a porta utilizada, nós usamos essa variável*/
        @Bean
        //Essa bean é exigida pela arquitetura do framework: https://docs.micronaut.io/latest/api/io/micronaut/context/annotation/Factory.html
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerServiceBlockingStub? {
            return KeyManagerServiceGrpc.newBlockingStub(channel)
        }
    }
}