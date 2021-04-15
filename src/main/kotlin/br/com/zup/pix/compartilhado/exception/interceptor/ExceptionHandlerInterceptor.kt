package br.com.zup.pix.compartilhado.exception.interceptor

import io.grpc.BindableService
import io.grpc.stub.StreamObserver
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

//Essa classe vai interceptar todos os erros onde contar o @ErrorHandler
@Singleton
class ExceptionHandlerInterceptor(@Inject private val procuraExceptionHandlerRespectivo: ProcuraExceptionHandlerRespectivo) :
    MethodInterceptor<BindableService, Any?> {

    private val logger = LoggerFactory.getLogger(this::class.java)


    override fun intercept(context: MethodInvocationContext<BindableService, Any?>): Any? {
        try {
            logger.info("Seguindo com o fluxo")
            return context.proceed()
        } catch (e: Exception) {
            //Quando ele encontrar o erro, ele vai verificar se temos alguma exception atribuída a ele no ProcuraExceptionHandler
            logger.error("Erro captado")
            val handler = procuraExceptionHandlerRespectivo.procura(e) as ExceptionHandler<Exception>

            //Uma vez encontrado o handler, é o momento de lançar o tratamento
            logger.error("Tratando erro")
            val status = handler.handle(e)

            logger.info("Erro tratado")
            GrpcEndpointArguments(context).response()
                .onError(status.status.asRuntimeException())

            return null
        }
    }

    /*
        Aqui estamos instanciando um genérico do controller do gRPC para especificar o que ele deve retornar,
         assim não precisamos especificar caso a caso versus exception por exception
    */
    private class GrpcEndpointArguments(val context: MethodInvocationContext<BindableService, Any?>) {

        fun response(): StreamObserver<*> {
            return context.parameterValues[1] as StreamObserver<*>
        }

    }
}