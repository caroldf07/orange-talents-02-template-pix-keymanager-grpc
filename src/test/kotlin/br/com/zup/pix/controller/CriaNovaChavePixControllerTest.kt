package br.com.zup.pix.controller

import br.com.zup.KeyManagerServiceGrpc
import br.com.zup.KeyManagerServiceGrpc.KeyManagerServiceBlockingStub
import br.com.zup.NovaChavePixRequest.*
import br.com.zup.NovaChavePixResponse
import br.com.zup.pix.model.ChavePix
import br.com.zup.pix.model.TipoChaveEnum.CPF
import br.com.zup.pix.model.TipoContaEnum.CONTA_CORRENTE
import br.com.zup.pix.repository.ChavePixRepository
import br.com.zup.sistemasExternos.model.DadosContaItau
import io.grpc.ManagedChannel
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

@MicronautTest(transactional = false)// Quando estamos trabalhando com servidor gRPC, ele roda em uma thread separada e, portanto, não participa a cada chamada do teste o que pode trazer problemas, por isso desligamos o transactional
internal class CriaNovaChavePixControllerTest(
    val repository: ChavePixRepository,
    val grpcClient: KeyManagerServiceBlockingStub
) {

    @Test
    @DisplayName("deve cadastrar uma nova chave")
    fun `deve cadastrar uma nova chave`() {
        //cenário
        repository.deleteAll()
        //ação
        val response: NovaChavePixResponse = grpcClient.criaChavePix(
            //Estamos simulando o envio de uma requisição, igual fazemos via Bloom
            newBuilder()
                .setIdentificadorItau("c56dfef4-7901-44fb-84e2-a2cefb157890")
                .setTipoChave(TipoChave.CPF)
                .setValorChave("02467781054")
                .setTipoConta(TipoConta.CONTA_CORRENTE)
                .build()
        )
        response.pixId
        //validação
        assertNotNull(response.pixId)
        assertTrue(repository.existsById(UUID.fromString(response.pixId))) // efeito colateral, estamos validando se a integração realmente ocorreu entre o sistema e o banco de dados

    }

    @Test
    @DisplayName("não deve adicionar nova chave quando chave já existente")
    fun `não deve adicionar nova chave quando chave já existente`() {
        //cenário
        repository.deleteAll() //limpamos o banco, caso o teste anterior o tenha sujado
        val existente = repository.save(
            ChavePix(
                UUID.fromString("c56dfef4-7901-44fb-84e2-a2cefb157890"), CPF, "02467781054", CONTA_CORRENTE,
                DadosContaItau("ITAÚ UNIBANCO S.A.", "Rafael M C Ponte", "02467781054", "0001", "291900")
            )
        )
        //ação
        assertThrows<StatusRuntimeException> {
            grpcClient.criaChavePix(
                newBuilder().setIdentificadorItau("c56dfef4-7901-44fb-84e2-a2cefb157890")
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .setTipoChave(TipoChave.CPF)
                    .setValorChave(existente.valorChave)
                    .build()
            )
        } //Como estamos esperando que ele dê um exceção, usamos o assertThrows

        //validação

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