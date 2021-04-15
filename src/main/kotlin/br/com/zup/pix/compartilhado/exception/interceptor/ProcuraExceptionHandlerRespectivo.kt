package br.com.zup.pix.compartilhado.exception.interceptor


import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProcuraExceptionHandlerRespectivo(@Inject private val listaHandlers: List<ExceptionHandler<*>>) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun procura(e: Exception): ExceptionHandler<*> {
        logger.info("Procurando tratamento para o erro")
        val procuraHandlers: List<ExceptionHandler<*>> = listaHandlers.filter { it.supports(e) } /*Faz o filter das exceptions para verificar se
        tem algum handler para aquela exception*/


        if (procuraHandlers.size > 1) {
            logger.error("Erro na busca do tratamento")
            throw IllegalArgumentException("Tem mais de um handler para a mesma exception")
        }

        logger.info("Tratamento encontrado")
        return procuraHandlers.first()
    }

}
