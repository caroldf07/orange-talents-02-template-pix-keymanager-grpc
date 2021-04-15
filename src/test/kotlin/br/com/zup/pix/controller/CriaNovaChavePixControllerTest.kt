package br.com.zup.pix.controller

import br.com.zup.KeyManagerServiceGrpc
import br.com.zup.KeyManagerServiceGrpc.KeyManagerServiceBlockingStub
import br.com.zup.NovaChavePixRequest.*
import io.grpc.ManagedChannel
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@MicronautTest
internal class CriaNovaChavePixControllerTest(val grpcClient: KeyManagerServiceBlockingStub) {

    @Test
    @DisplayName("deve cadastrar uma nova chave")
    fun `deve cadastrar uma nova chave`() {
        //cenário
        //ação
        val response = grpcClient.criaChavePix(
            //Estamos simulando o envio de uma requisição, igual fazemos via Bloom
            newBuilder()
                .setIdentificadorItau("c56dfef4-7901-44fb-84e2-a2cefb157890")
                .setTipoChave(TipoChave.CPF)
                .setValorChave("02467781054")
                .setTipoConta(TipoConta.CONTA_CORRENTE)
                .build()
        )
        //validação
        assertNotNull(response.pixId)

    }

    //Aqui nós fabricamos o nosso client para que possamos testar a integração para a requisição no gRPC
    @Factory
    class Clients {
        /*Usamos o GrpcServerChannel.NAME, pois, a cada teste, o Micronaut muda a porta do servidor que ele usa, então
        Como não temos como saber qual será a porta utilizada, nós usamos essa variável*/
        @Bean
        //Sem o @Bean, ele não sobre a chamada ao servidor e dá como erro no constructor
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerServiceBlockingStub? {
            return KeyManagerServiceGrpc.newBlockingStub(channel)
        }
    }
}