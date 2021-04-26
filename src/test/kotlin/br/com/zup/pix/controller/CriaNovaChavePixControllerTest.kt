package br.com.zup.pix.controller

import br.com.zup.KeyManagerServiceGrpc
import br.com.zup.NovaChavePixRequest.*
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
import br.com.zup.sistemasExternos.model.DadosContaItau
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
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)// Quando estamos trabalhando com servidor gRPC, ele roda em uma thread separada e, portanto, não participa a cada chamada do teste o que pode trazer problemas, por isso desligamos o transactional
internal class CriaNovaChavePixControllerTest(
    @Inject val grpcClient: KeyManagerServiceGrpc.KeyManagerServiceBlockingStub,
    @Inject val repository: ChavePixRepository
) {
    /*
    * Testes a serem feitos:
    * 1 - Happy Path - ok
    * 2 - Não pode cadastrar nova chave quando campo inválido - ok
    * 3 - Não pode cadastrar nova chave quando chave já cadastrada no banco de dados - ok
    * 4 - Não pode cadastrar nova chave quando cliente não consta no Itaú - ok
    * 5 - Não pode cadastrar nova chave quando chave já cadastrada no Bcb - ok
    * */


    @Inject
    lateinit var itauClient: ItauClient

    @Inject
    lateinit var bcbClient: BcbClient

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

    @MockBean(ItauClient::class)
    fun validaCliente(): ItauClient {
        return mock(ItauClient::class.java)
    }

    @MockBean(BcbClient::class)
    fun cadastraChavePix(): BcbClient {
        return mock(BcbClient::class.java)
    }

    val dadosContaItauResponse = br.com.zup.sistemasExternos.model.DadosContaItauResponse(
        tipo = tipoConta.toString(),
        br.com.zup.sistemasExternos.model.InstituicaoResponse(nome = instituicao, ispb = ispb),
        agencia = agencia,
        numero = numeroConta,
        br.com.zup.sistemasExternos.model.TitularResponse(nome = nomeTitular, cpf = cpf)
    )

    val bcbRequest = BcbRequest(
        keyType = KeyTypeEnum.by(tipoChave),
        key = valorChave,
        BankAccountRequest(
            participant = ispb,
            branch = agencia,
            accountNumber = numeroConta,
            accountType = AccountTypeEnum.by(tipoConta)
        ), OwnerRequest(TypeEnum.NATURAL_PERSON, name = nomeTitular, taxIdNumber = cpf)
    )

    val bcbResponse = BcbResponse(key = valorChave, createdAt = LocalDateTime.now())

    @AfterEach
    fun clean() {
        repository.deleteAll()
    }

    @Test
    @DisplayName("deve cadastrar nova chave pix")
    fun `deve cadastrar nova chave pix`() {
        //cenário

        `when`(itauClient.validaCliente(identificadorItau.toString(), tipoConta.toString())).thenReturn(
            HttpResponse.ok(
                dadosContaItauResponse
            )
        )

        `when`(bcbClient.cadastraChavePix(bcbRequest)).thenReturn(HttpResponse.created(bcbResponse))


        //ação
        val response = grpcClient.criaChavePix(
            newBuilder().setIdentificadorItau(identificadorItau.toString())
                .setValorChave(valorChave)
                .setTipoChave(TipoChave.CPF).setTipoConta(TipoConta.CONTA_CORRENTE).build()
        )
        //validação
        assertNotNull(response)
        assertTrue(repository.existsByValorChave(valorChave))
    }

    @Test
    @DisplayName("não deve cadastrar nova chave quando campo inválido")
    fun `não deve cadastrar nova chave quando campo inválido`() {
        //cenário

        //ação
        val response = assertThrows<StatusRuntimeException> {
            grpcClient.criaChavePix(
                newBuilder().setIdentificadorItau("")
                    .setValorChave("")
                    .setTipoChave(TipoChave.CHAVE_UNKNOWN).setTipoConta(TipoConta.CONTA_UNKNOWN).build()
            )
        }
        //validação
        assertEquals(Status.INVALID_ARGUMENT.code, response.status.code)
        assertTrue(response.message!!.contains("não deve ser nulo"))
        assertTrue(response.message!!.contains("registra.novaChave.identificadorItau: não deve estar em branco"))
        assertTrue(response.message!!.contains("registra.novaChave: chave Pix inválida ()"))
        assertTrue(response.message!!.contains("registra.novaChave.tipoChave: não deve ser nulo"))
        assertTrue(response.message!!.contains("registra.novaChave.identificadorItau: não deve estar em branco"))
        assertTrue(response.message!!.contains("registra.novaChave.tipoConta: não deve ser nulo"))
    }

    @Test
    @DisplayName("não pode cadastrar nova chave quando chave já cadastrada no banco de dados")
    fun `não pode cadastrar nova chave quando chave já cadastrada no banco de dados`() {
        //cenário
        val chave = repository.save(
            ChavePix(
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
        )

        //ação
        val response = assertThrows<StatusRuntimeException> {
            grpcClient.criaChavePix(
                newBuilder().setIdentificadorItau(identificadorItau.toString())
                    .setValorChave(valorChave)
                    .setTipoChave(TipoChave.CPF).setTipoConta(TipoConta.CONTA_CORRENTE).build()
            )
        }

        //validação
        assertEquals(Status.ALREADY_EXISTS.code, response.status.code)
        assertTrue(response.message!!.contains("Chave já cadastrada"))
    }

    @Test
    @DisplayName("não pode cadastrar nova chave quando cliente não consta no Itaú")
    fun `não pode cadastrar nova chave quando cliente não consta no Itaú`() {
        //cenário
        `when`(
            itauClient.validaCliente(
                identificadorItau = identificadorItau.toString(),
                tipo = tipoConta.toString()
            )
        ).thenThrow(HttpClientResponseException::class.java)
        //ação

        val response = assertThrows<StatusRuntimeException> {
            grpcClient.criaChavePix(
                newBuilder().setIdentificadorItau(identificadorItau.toString())
                    .setValorChave(valorChave)
                    .setTipoChave(TipoChave.CPF).setTipoConta(TipoConta.CONTA_CORRENTE).build()
            )
        }

        //validação
        assertEquals(Status.INVALID_ARGUMENT.code, response.status.code)
        assertTrue(response.message!!.contains("Cliente inexistente"))
    }

    @Test
    @DisplayName("não pode cadastrar nova chave quando chave já cadastrada no Bcb")
    fun `não pode cadastrar nova chave quando chave já cadastrada no Bcb`() {
        //cenário
        `when`(
            itauClient.validaCliente(
                identificadorItau = identificadorItau.toString(),
                tipo = tipoConta.toString()
            )
        ).thenReturn(
            HttpResponse.ok(dadosContaItauResponse)
        )

        `when`(bcbClient.cadastraChavePix(bcbRequest = bcbRequest)).thenReturn(HttpResponse.unprocessableEntity())
        //ação

        val response = assertThrows<StatusRuntimeException> {
            grpcClient.criaChavePix(
                newBuilder().setIdentificadorItau(identificadorItau.toString())
                    .setValorChave(valorChave)
                    .setTipoChave(TipoChave.CPF).setTipoConta(TipoConta.CONTA_CORRENTE).build()
            )
        }

        //validação
        assertEquals(Status.FAILED_PRECONDITION.code, response.status.code)
        assertTrue(response.message!!.contains("Falha na requisição"))
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
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerServiceGrpc.KeyManagerServiceBlockingStub? {
            return KeyManagerServiceGrpc.newBlockingStub(channel)
        }
    }

}