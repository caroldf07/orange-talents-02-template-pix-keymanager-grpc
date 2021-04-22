package br.com.zup.pix.controller

import br.com.zup.KeyManagerServiceGrpc
import br.com.zup.KeyManagerServiceGrpc.KeyManagerServiceBlockingStub
import br.com.zup.NovaChavePixRequest.*
import br.com.zup.NovaChavePixResponse
import br.com.zup.pix.model.ChavePix
import br.com.zup.pix.model.TipoChaveEnum
import br.com.zup.pix.model.TipoContaEnum
import br.com.zup.pix.repository.ChavePixRepository
import br.com.zup.sistemasExternos.client.BcbClient
import br.com.zup.sistemasExternos.client.ItauClient
import br.com.zup.sistemasExternos.dominio.BankAccountRequest
import br.com.zup.sistemasExternos.dominio.BcbRequest
import br.com.zup.sistemasExternos.dominio.BcbResponse
import br.com.zup.sistemasExternos.dominio.OwnerRequest
import br.com.zup.sistemasExternos.model.AccountTypeEnum
import br.com.zup.sistemasExternos.model.KeyTypeEnum
import br.com.zup.sistemasExternos.model.TypeEnum
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
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

/*
* 1 - Happy path - ok
* 2 - Chave já cadastrada no Itaú
* 3 - Chave já cadastrada no Bcb
* 4 - Qualquer campo inválido - ok
* 5 - Client do Itaú não encontra a chave
* 6 - Client do Bcb não encontra a chave
* */

@MicronautTest(transactional = false)// Quando estamos trabalhando com servidor gRPC,
// ele roda em uma thread separada e, portanto, não participa a cada chamada do teste complicando tanto a instância do banco como gerando falsos positivos
internal class CriaNovaChavePixControllerTest(
    private val repository: ChavePixRepository,
    private val grpcClient: KeyManagerServiceBlockingStub
) {

    companion object {
        val IDENTIFICADORITAU = UUID.randomUUID().toString()
    }

    @Inject
    lateinit var itauClient: ItauClient

    @Inject
    lateinit var bcbClient: BcbClient

    private val dadosContaItauResponse =
        br.com.zup.sistemasExternos.model.DadosContaItauResponse(
            tipo = "CONTA_CORRENTE",
            instituicao = br.com.zup.sistemasExternos.model.InstituicaoResponse(
                nome = "ITAÚ UNIBANCO S.A.",
                ispb = "60701190"
            ),
            agencia = "0001",
            numero = "212233",
            titular = br.com.zup.sistemasExternos.model.TitularResponse(nome = "Alberto Tavares", cpf = "06628726061")
        )

    private val bcbResponse = BcbResponse(key = "06628726061", createdAt = LocalDateTime.now())

    @MockBean(ItauClient::class) //mockamos a chamada para o cliente externo, pois as vezes pode demorar demais a chamada ou então fazer algum tipo de registro que, no momento do teste, não é para acontecer
    fun validaCliente(): ItauClient? {
        return mock(ItauClient::class.java)
    }

    @MockBean(BcbClient::class)
    fun cadastraChavePix(): BcbClient? {
        return mock(BcbClient::class.java)
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
                identificadorItau = IDENTIFICADORITAU,
                tipo = TipoContaEnum.CONTA_CORRENTE.toString()
            )
        ).thenReturn(
            HttpResponse.ok(dadosContaItauResponse)
        )

        `when`(
            bcbClient.cadastraChavePix(
                BcbRequest(
                    keyType = KeyTypeEnum.CPF,
                    key = "06628726061",
                    BankAccountRequest(
                        participant = "60701190",
                        branch = "0001",
                        accountType = AccountTypeEnum.CACC,
                        accountNumber = "212233"
                    ),
                    OwnerRequest(type = TypeEnum.NATURAL_PERSON, name = "Alberto Tavares", taxIdNumber = "06628726061")
                )
            )
        ).thenReturn(HttpResponse.created(bcbResponse))

        //ação
        val response: NovaChavePixResponse = grpcClient.criaChavePix(
            //Estamos simulando o envio de uma requisição, igual fazemos via Bloom
            newBuilder()
                .setIdentificadorItau(IDENTIFICADORITAU)
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
    @DisplayName("não deve adicionar nova chave quando chave já existente no nosso sistema")
    fun `não deve adicionar nova chave quando chave já existente no nosso sistema`() {
        //cenário

        val existente = repository.save(
            ChavePix(
                identificadorItau = UUID.fromString(IDENTIFICADORITAU),
                tipoChave = TipoChaveEnum.CPF,
                valorChave = "06628726061",
                tipoConta = TipoContaEnum.CONTA_CORRENTE, conta = dadosContaItauResponse.toModel()
            )
        )
        //ação
        val erro = assertThrows<StatusRuntimeException> {
            grpcClient.criaChavePix(
                newBuilder().setIdentificadorItau(IDENTIFICADORITAU)
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

        `when`(
            itauClient.validaCliente(
                identificadorItau = "1",
                tipo = TipoContaEnum.CONTA_CORRENTE.toString()
            )
        ).thenThrow(
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