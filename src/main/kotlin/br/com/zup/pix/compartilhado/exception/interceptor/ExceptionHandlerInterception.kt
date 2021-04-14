package br.com.zup.pix.compartilhado.exception.interceptor

import io.grpc.BindableService
import io.grpc.stub.StreamObserver
import io.micronaut.aop.Interceptor
import io.micronaut.aop.InvocationContext
import io.micronaut.aop.MethodInvocationContext
import javax.inject.Inject

//Essa classe vai interceptar todos os erros onde contar o @ErrorHandler
class ExceptionHandlerInterception(@Inject private val procuraExceptionHandlerRespectivo: ProcuraExceptionHandlerRespectivo) :
    Interceptor<BindableService, Any?> {


    override fun intercept(context: InvocationContext<BindableService, Any?>): Any? {
        try {
            return context.proceed()
        } catch (e: Exception) {
            //Quando ele encontrar o erro, ele vai verificar se temos alguma exception atribuída a ele no ProcuraExceptionHandler

            val handler = procuraExceptionHandlerRespectivo.procura(e) as ExceptionHandler<Exception>

            //Uma vez encontrado o handler, é o momento de lançar o tratamento
            val status = handler.handle(e)

            GrpcEndpointArguments(context as MethodInvocationContext<BindableService, Any?>).response()
                .onError(status.asRuntimeException())

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