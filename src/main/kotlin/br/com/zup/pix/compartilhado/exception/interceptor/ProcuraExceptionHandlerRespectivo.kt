package br.com.zup.pix.compartilhado.exception.interceptor


import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProcuraExceptionHandlerRespectivo(@Inject private val listaHandlers: List<ExceptionHandler<*>>) {
    fun procura(e: Exception): ExceptionHandler<*> {
        val procuraHandlers = listaHandlers.filter { it.supports(e) } /*Faz o filter das exceptions para verificar se
        tem algum handler para aquela exception*/
        if (procuraHandlers.size > 1) {
            throw IllegalArgumentException("Tem mais de um handler para a mesma exception")
        }

        return procuraHandlers.first()
    }

}
